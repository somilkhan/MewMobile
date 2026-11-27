#!/usr/bin/env bash

set -euo pipefail

from_ref=""
to_ref="HEAD"
repository="${GITHUB_REPOSITORY:-}"
offline=false
first_parent=false
exclude_commits="${RELEASE_NOTES_EXCLUDE_COMMITS:-}"
exclude_refs=()
upstream_ref=""
upstream_repository=""

usage() {
    echo "Usage: $0 --from <commit> [--to <commit>] [--repository <owner/repo>] [--exclude <hashes>] [--exclude-ref <ref>]... [--offline] [--first-parent]" >&2
}

while [[ $# -gt 0 ]]; do
    case "$1" in
        --from)
            from_ref="${2:-}"
            shift 2
            ;;
        --to)
            to_ref="${2:-}"
            shift 2
            ;;
        --repository)
            repository="${2:-}"
            shift 2
            ;;
        --exclude)
            exclude_commits="${exclude_commits} ${2:-}"
            shift 2
            ;;
        --exclude-ref)
            exclude_refs+=("^${2:-}")
            shift 2
            ;;
        --upstream-ref)
            upstream_ref="${2:-}"
            shift 2
            ;;
        --upstream-repository)
            upstream_repository="${2:-}"
            shift 2
            ;;
        --offline)
            offline=true
            shift
            ;;
        --first-parent)
            first_parent=true
            shift
            ;;
        -h|--help)
            usage
            exit 0
            ;;
        *)
            usage
            exit 1
            ;;
    esac
done

if [[ -z "$from_ref" ]]; then
    usage
    exit 1
fi

git cat-file -e "${from_ref}^{commit}" 2>/dev/null || {
    echo "Unknown starting commit: ${from_ref}" >&2
    exit 1
}
git cat-file -e "${to_ref}^{commit}" 2>/dev/null || {
    echo "Unknown ending commit: ${to_ref}" >&2
    exit 1
}

is_excluded_hash() {
    local commit="$1"
    local excluded
    for excluded in ${exclude_commits//,/ }; do
        [[ -n "$excluded" ]] || continue
        if [[ "$commit" == "$excluded"* ]]; then
            return 0
        fi
    done
    return 1
}

is_release_note() {
    local subject_lower
    local version_bump_pattern='^(bump([[:space:]].*)?version|version[[:space:]]+bump)([[:space:]].*)?$'
    local cleanup_pattern='^cleanup([[:space:][:punct:]].*)?$'
    local conventional_noise_pattern='^(build|chore|ci|docs|style|test)(\([^)]*\))?:'
    subject_lower="$(printf '%s' "$1" | tr '[:upper:]' '[:lower:]')"

    [[ "$subject_lower" != *"[skip release notes]"* ]] || return 1
    [[ ! "$subject_lower" =~ $version_bump_pattern ]] || return 1
    [[ ! "$subject_lower" =~ $cleanup_pattern ]] || return 1
    [[ ! "$subject_lower" =~ $conventional_noise_pattern ]] || return 1
    # Reverts and upstream-merge repair commits describe how a merge was done, not what users get.
    local merge_noise_pattern='^(revert|merge)([[:space:][:punct:]].*)?$|\(round [0-9]+\)|^fix\(merge\)|(after|from) (the )?[a-z0-9 ._-]*merge|^add files via upload'
    [[ ! "$subject_lower" =~ $merge_noise_pattern ]] || return 1
    return 0
}

resolve_username() {
    local commit="$1"
    local author_name="$2"
    local author_email="$3"
    local username=""

    if [[ "$author_email" =~ ^[0-9]+\+([^@]+)@users\.noreply\.github\.com$ ]]; then
        username="${BASH_REMATCH[1]}"
    elif [[ "$author_email" =~ ^([^@]+)@users\.noreply\.github\.com$ ]]; then
        username="${BASH_REMATCH[1]}"
    elif [[ "$offline" == false && -n "$repository" && -n "${GH_TOKEN:-}" ]] && command -v gh >/dev/null 2>&1; then
        username="$(gh api "repos/${repository}/commits/${commit}" --jq '.author.login // empty' 2>/dev/null || true)"
        if [[ -z "$username" ]]; then
            username="$(
                gh api \
                    -H 'Accept: application/vnd.github+json' \
                    "repos/${repository}/commits/${commit}/pulls" \
                    --jq '.[0].user.login // empty' \
                    2>/dev/null \
                    || true
            )"
        fi
    fi

    if [[ -n "$username" ]]; then
        # Only real GitHub logins get an @ so release notes never mention unrelated accounts.
        printf '@%s' "$username"
    else
        printf '%s' "${author_name:-unknown}"
    fi
}

resolve_pull_request() {
    local commit="$1"
    local repository="${2:-$repository}"

    if [[ "$offline" == true || -z "$repository" || -z "${GH_TOKEN:-}" ]] || ! command -v gh >/dev/null 2>&1; then
        return
    fi

    gh api \
        -H 'Accept: application/vnd.github+json' \
        "repos/${repository}/commits/${commit}/pulls" \
        --jq '[.[] | select(.merged_at != null)][0] | if . == null then empty else [.number, .title, (.user.login // "unknown")] | @tsv end' \
        2>/dev/null \
        || true
}

seen_subjects=$'\n'
seen_pull_requests=$'\n'
separator=$'\x1f'

emit_notes() {
    local notes_repository="$1"
    shift
    while IFS="$separator" read -r commit short_hash subject author_name author_email; do
    [[ -n "$commit" ]] || continue
    is_excluded_hash "$commit" && continue

    pull_request="$(resolve_pull_request "$commit" "$notes_repository")"
    if [[ -n "$pull_request" ]]; then
        pull_request_number=""
        pull_request_title=""
        pull_request_username=""
        IFS=$'\t' read -r pull_request_number pull_request_title pull_request_username <<< "$pull_request"
        if [[ -n "$pull_request_number" ]]; then
            [[ "$seen_pull_requests" != *$'\n'"$pull_request_number"$'\n'* ]] || continue
            seen_pull_requests+="${pull_request_number}"$'\n'
            is_release_note "$pull_request_title" || continue
            display_pull_request_title="$(printf '%s' "$pull_request_title" | sed -E 's/[[:space:]]+$//')"
            printf -- '- [%s (#%s)](https://github.com/%s/pull/%s) @%s  \n' \
                "$display_pull_request_title" \
                "$pull_request_number" \
                "$notes_repository" \
                "$pull_request_number" \
                "${pull_request_username:-unknown}"
            continue
        fi
    fi

    is_release_note "$subject" || continue

    normalized_subject="$(printf '%s' "$subject" | tr '[:upper:]' '[:lower:]' | sed -E 's/[[:space:]]+/ /g; s/[[:space:].]+$//')"
    [[ "$seen_subjects" != *$'\n'"$normalized_subject"$'\n'* ]] || continue
    seen_subjects+="${normalized_subject}"$'\n'

    display_subject="$(printf '%s' "$subject" | sed -E 's/[[:space:]]*\[(build-apk|release-[a-z]+|skip ci)\]//g; s/[[:space:]]+$//; s/\.$//')"
    username="$(resolve_username "$commit" "$author_name" "$author_email")"
    printf -- '- %s %s %s  \n' "$short_hash" "$display_subject" "$username"
done < <(
        git log "$@" --no-merges ${first_parent_args[@]+"${first_parent_args[@]}"} \
            --format="%H${separator}%h${separator}%s${separator}%an${separator}%ae"
    )
}

# --first-parent lists only commits made on this repository's own line of history.
# Upstream work that arrives through merge commits is summarised in CHANGELOG.md instead.
first_parent_args=()
if [[ "$first_parent" == true ]]; then
    first_parent_args=(--first-parent)
fi

upstream_args=()
upstream_base=""
if [[ -n "$upstream_ref" ]] && git cat-file -e "${upstream_ref}^{commit}" 2>/dev/null; then
    upstream_args=("^${upstream_ref}")
    upstream_base="$(git merge-base "$to_ref" "$upstream_ref" || true)"
fi

# Drop work that was undone inside the release range so notes only list what ships:
# commits named by "This reverts commit <sha>" and commits rolled back by a
# "restore <branch> to last green <sha>" reset (everything after <sha> up to the reset).
collect_undone_commits() {
    local range_args=("$@")
    local commit subject body target
    while IFS= read -r commit; do
        subject="$(git log -1 --format=%s "$commit")"
        body="$(git log -1 --format=%B "$commit")"
        while read -r target; do
            [[ -n "$target" ]] && printf '%s\n' "$target"
        done < <(printf '%s\n' "$body" | sed -nE 's/.*This reverts commit ([0-9a-f]{7,40}).*/\1/p')
        target="$(printf '%s' "$subject" | sed -nE 's/.*[Rr]estore [^ ]+ to last green (commit |tree )?\(?([0-9a-f]{7,40})\)?.*/\2/p')"
        if [[ -n "$target" ]] && git cat-file -e "${target}^{commit}" 2>/dev/null; then
            git rev-list ${first_parent_args[@]+"${first_parent_args[@]}"} "${target}..${commit}^"
        fi
    done < <(git rev-list ${first_parent_args[@]+"${first_parent_args[@]}"} "${range_args[@]}")
}
exclude_commits="${exclude_commits} $(collect_undone_commits "${from_ref}..${to_ref}" | tr '\n' ' ')"

fork_notes="$(emit_notes "$repository" "${from_ref}..${to_ref}" ${exclude_refs[@]+"${exclude_refs[@]}"} ${upstream_args[@]+"${upstream_args[@]}"})"
upstream_notes=""
if [[ -n "$upstream_base" ]]; then
    upstream_notes="$(emit_notes "${upstream_repository:-$repository}" "$upstream_base" "^${from_ref}")"
fi

if [[ -n "$upstream_notes" ]]; then
    if [[ -n "$fork_notes" ]]; then
        printf '### Nuvio Enhanced\n\n%s\n\n' "$fork_notes"
    fi
    printf '### Upstream (%s)\n\n%s\n' "${upstream_repository:-upstream}" "$upstream_notes"
elif [[ -n "$fork_notes" ]]; then
    printf '%s\n' "$fork_notes"
fi
