import AVKit
import Combine
import SwiftUI
import UIKit
import SwiftUI
import ComposeApp
import ImageIO

private enum NuvioNativeTabIcon {
    struct ProfileAvatarAnimation {
        let frames: [UIImage]
        let duration: TimeInterval
    }

    static let home = vectorIcon(
        viewport: CGSize(width: 24, height: 24),
        paths: [
            "M10,20V14H14V20H19V12H22L12,3L2,12H5V20Z",
        ]
    )

let nuvioPlayerImmersiveSystemUIVisibilityDidChange = Notification.Name(
    "NuvioPlayerImmersiveSystemUIVisibilityDidChange"
)
let nuvioPlayerImmersiveSystemUIVisibleKey = "isVisible"

private enum NuvioComposeHost {
    static let registerPlayerBridge: Void = {
        NuvioPlayerRegistration.register()
    }()

    static func wrap(
        _ contentController: UIViewController,
        backgroundColor: UIColor = nuvioBackgroundColor,
        disablesInteractiveContentPopGesture: Bool = false,
        passesThroughEmptyNavigationBarAreas: Bool = false,
        hidesContainingTabBar: Bool = false,
        verticalBar: VerticalBar? = nil,
        hinge: Hinge? = nil,
        onTabBarControllerAvailable: ((UITabBarController) -> Void)? = nil
    ) -> RootComposeViewController {
        _ = registerPlayerBridge
        contentController.view.backgroundColor = backgroundColor
        return RootComposeViewController(
            contentController: contentController,
            backgroundColor: backgroundColor,
            disablesInteractiveContentPopGesture: disablesInteractiveContentPopGesture,
            passesThroughEmptyNavigationBarAreas: passesThroughEmptyNavigationBarAreas,
            hidesContainingTabBar: hidesContainingTabBar,
            verticalBar: verticalBar,
            hinge: hinge,
            onTabBarControllerAvailable: onTabBarControllerAvailable
        )
        image.draw(in: drawRect)
    }

    private struct SVGPath {
        private enum Token {
            case command(Character)
            case number(CGFloat)
        }

        let data: String

        var cgPath: CGPath? {
            let tokens = Self.tokens(from: data)
            var index = 0
            var command: Character?
            var current = CGPoint.zero
            var subpathStart = CGPoint.zero
            let path = CGMutablePath()

            func hasNumber() -> Bool {
                guard index < tokens.count else { return false }
                if case .number = tokens[index] { return true }
                return false
            }

            func readNumber() -> CGFloat? {
                guard index < tokens.count else { return nil }
                guard case let .number(value) = tokens[index] else { return nil }
                index += 1
                return value
            }

            func readPoint(relative: Bool) -> CGPoint? {
                guard let x = readNumber(), let y = readNumber() else { return nil }
                let point = CGPoint(x: x, y: y)
                return relative ? CGPoint(x: current.x + point.x, y: current.y + point.y) : point
            }

            while index < tokens.count {
                if case let .command(value) = tokens[index] {
                    command = value
                    index += 1
                }

                guard let activeCommand = command else { return nil }
                let relative = activeCommand.isLowercase

                switch activeCommand.uppercased() {
                case "M":
                    guard let point = readPoint(relative: relative) else { return nil }
                    path.move(to: point)
                    current = point
                    subpathStart = point
                    command = relative ? "l" : "L"
                case "L":
                    while hasNumber() {
                        guard let point = readPoint(relative: relative) else { return nil }
                        path.addLine(to: point)
                        current = point
                    }
                case "H":
                    while hasNumber() {
                        guard let x = readNumber() else { return nil }
                        let point = CGPoint(x: relative ? current.x + x : x, y: current.y)
                        path.addLine(to: point)
                        current = point
                    }
                case "V":
                    while hasNumber() {
                        guard let y = readNumber() else { return nil }
                        let point = CGPoint(x: current.x, y: relative ? current.y + y : y)
                        path.addLine(to: point)
                        current = point
                    }
                case "C":
                    while hasNumber() {
                        guard
                            let c1 = readPoint(relative: relative),
                            let c2 = readPoint(relative: relative),
                            let end = readPoint(relative: relative)
                        else { return nil }
                        path.addCurve(to: end, control1: c1, control2: c2)
                        current = end
                    }
                case "Z":
                    path.closeSubpath()
                    current = subpathStart
                default:
                    return nil
                }
            }

            return path
        }

        private static func tokens(from data: String) -> [Token] {
            let pattern = "[MmLlHhVvCcZz]|[-+]?(?:\\d*\\.\\d+|\\d+\\.?)(?:[eE][-+]?\\d+)?"
            guard let regex = try? NSRegularExpression(pattern: pattern) else { return [] }
            let range = NSRange(data.startIndex..<data.endIndex, in: data)
            return regex.matches(in: data, range: range).compactMap { match in
                guard let tokenRange = Range(match.range, in: data) else { return nil }
                let token = String(data[tokenRange])
                if token.count == 1, let character = token.first, character.isLetter {
                    return .command(character)
                }
                guard let value = Double(token) else { return nil }
                return .number(CGFloat(value))
            }
        }
    }
}

final class RootComposeViewController: UIViewController {
    private let contentController: UIViewController
    private let backgroundColor: UIColor
    private let disablesInteractiveContentPopGesture: Bool
    private let passesThroughEmptyNavigationBarAreas: Bool
    private let hidesContainingTabBar: Bool
    private let hinge: Hinge?
    private let onTabBarControllerAvailable: ((UITabBarController) -> Void)?
    private var immersiveSystemUIObserver: NSObjectProtocol?
    private let verticalBar: VerticalBar?
    private var leadingConstraint: NSLayoutConstraint?
    private var trailingConstraint: NSLayoutConstraint?
    private var isHalfOpen = false
    private var foldBoundsSize = CGSize.zero

    init(
        contentController: UIViewController,
        backgroundColor: UIColor,
        disablesInteractiveContentPopGesture: Bool,
        passesThroughEmptyNavigationBarAreas: Bool = false,
        hidesContainingTabBar: Bool,
        verticalBar: VerticalBar?,
        hinge: Hinge?,
        onTabBarControllerAvailable: ((UITabBarController) -> Void)?
    ) {
        self.contentController = contentController
        self.backgroundColor = backgroundColor
        self.disablesInteractiveContentPopGesture = disablesInteractiveContentPopGesture
        self.passesThroughEmptyNavigationBarAreas = passesThroughEmptyNavigationBarAreas
        self.hidesContainingTabBar = hidesContainingTabBar
        self.verticalBar = verticalBar
        self.hinge = hinge
        self.onTabBarControllerAvailable = onTabBarControllerAvailable
        super.init(nibName: nil, bundle: nil)
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    override func viewDidLoad() {
        super.viewDidLoad()

        view.backgroundColor = backgroundColor
        view.isOpaque = backgroundColor != .clear
        contentController.view.backgroundColor = backgroundColor

        addChild(contentController)
        view.addSubview(contentController.view)
        contentController.view.translatesAutoresizingMaskIntoConstraints = false
        let leadingConstraint = contentController.view.leadingAnchor.constraint(equalTo: view.leadingAnchor)
        let trailingConstraint = contentController.view.trailingAnchor.constraint(equalTo: view.trailingAnchor)
        NSLayoutConstraint.activate([
            contentController.view.topAnchor.constraint(equalTo: view.topAnchor),
            contentController.view.bottomAnchor.constraint(equalTo: view.bottomAnchor),
            leadingConstraint,
            trailingConstraint,
        ])
        self.leadingConstraint = leadingConstraint
        self.trailingConstraint = trailingConstraint
        if #available(iOS 27.1, *) {
            registerForTraitChanges(UITraitCollection.systemTraitsAffectingVerticalBarEdge) {
                (controller: RootComposeViewController, _: UITraitCollection) in
                controller.updateVerticalBar()
            }
            if hinge != nil {
                view.addInteraction(UIHingeInteraction { [weak self] _, update in
                    self?.isHalfOpen = update.hinge?.status == .partiallyOpen
                    self?.updateFold()
                })
            }
        }
        contentController.didMove(toParent: self)

        immersiveSystemUIObserver = NotificationCenter.default.addObserver(
            forName: nuvioPlayerImmersiveSystemUIVisibilityDidChange,
            object: nil,
            queue: .main
        ) { [weak self] _ in
            self?.refreshImmersiveSystemUI()
        }
    }

    deinit {
        if let immersiveSystemUIObserver {
            NotificationCenter.default.removeObserver(immersiveSystemUIObserver)
        }
    }

    override func viewSafeAreaInsetsDidChange() {
        super.viewSafeAreaInsetsDidChange()
        updateVerticalBar()
    }

    override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        enforceContainingTabBarVisibility()
        removeBarBlur()
        guard view.bounds.size != foldBoundsSize else { return }
        foldBoundsSize = view.bounds.size
        updateFold()
    }

    private func updateFold() {
        guard let hinge else { return }
        guard #available(iOS 27.1, *), isHalfOpen,
              let fold = view.reservedRegions(kind: .division).first(where: { $0.frame.width > $0.frame.height })
        else {
            hinge.clear()
            return
        }
        hinge.update(top: Double(fold.frame.minY), bottom: Double(fold.frame.maxY))
    }

    private func updateVerticalBar() {
        let insets = verticalBarInsets
        if let verticalBar {
            verticalBar.update(start: Double(insets.leading), end: Double(insets.trailing))
        } else {
            leadingConstraint?.constant = insets.leading
            trailingConstraint?.constant = -insets.trailing
        }
    }

    private var verticalBarInsets: NSDirectionalEdgeInsets {
        guard #available(iOS 27.1, *) else { return .zero }
        let insets = view.safeAreaInsets
        let isRightToLeft = view.effectiveUserInterfaceLayoutDirection == .rightToLeft
        switch traitCollection.verticalBarEdge {
        case .leading:
            return NSDirectionalEdgeInsets(top: 0, leading: isRightToLeft ? insets.right : insets.left, bottom: 0, trailing: 0)
        case .trailing:
            return NSDirectionalEdgeInsets(top: 0, leading: 0, bottom: 0, trailing: isRightToLeft ? insets.left : insets.right)
        default:
            return .zero
        }
    }

    override var childForHomeIndicatorAutoHidden: UIViewController? {
        nil
    }

    override var childForScreenEdgesDeferringSystemGestures: UIViewController? {
        nil
    }

    override var childForStatusBarHidden: UIViewController? {
        nil
    }

    override var prefersHomeIndicatorAutoHidden: Bool {
        SystemUI.shared.activePlayer?.prefersHomeIndicatorAutoHidden
            ?? immersiveController(in: contentController)?.prefersHomeIndicatorAutoHidden
            ?? false
    }

    override var preferredScreenEdgesDeferringSystemGestures: UIRectEdge {
        SystemUI.shared.activePlayer?.preferredScreenEdgesDeferringSystemGestures
            ?? immersiveController(in: contentController)?.preferredScreenEdgesDeferringSystemGestures
            ?? []
    }

    override var prefersStatusBarHidden: Bool {
        SystemUI.shared.activePlayer?.prefersStatusBarHidden
            ?? immersiveController(in: contentController)?.prefersStatusBarHidden
            ?? false
    }

    override var preferredStatusBarStyle: UIStatusBarStyle {
        .lightContent
    }

    override var preferredStatusBarUpdateAnimation: UIStatusBarAnimation {
        .fade
    }

    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        refreshContainingTabBarVisibility()
        refreshImmersiveSystemUI()
        setInteractiveContentPopGestureEnabled(false)
        setNavigationBarPassthroughEnabled(true)
        removeBarBlur()
    }

    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        refreshContainingTabBarVisibility()
        refreshImmersiveSystemUI()
        setInteractiveContentPopGestureEnabled(false)
        removeBarBlur()
        if let tabBarController {
            onTabBarControllerAvailable?(tabBarController)
        }
    }

    override func viewWillDisappear(_ animated: Bool) {
        setNavigationBarPassthroughEnabled(false)
        setInteractiveContentPopGestureEnabled(true)
        super.viewWillDisappear(animated)
    }

    func prepareForPictureInPictureParking() {
        setNavigationBarPassthroughEnabled(false)
        setInteractiveContentPopGestureEnabled(true)
    }

    func restoreAfterPictureInPictureParking() {
        refreshContainingTabBarVisibility()
        refreshImmersiveSystemUI()
        setInteractiveContentPopGestureEnabled(false)
        setNavigationBarPassthroughEnabled(true)
    }

    func refreshContainingTabBarVisibility() {
        enforceContainingTabBarVisibility()
        DispatchQueue.main.async { [weak self] in
            self?.enforceContainingTabBarVisibility()
        }
    }

    private func enforceContainingTabBarVisibility() {
        guard hidesContainingTabBar, let tabBar = tabBarController?.tabBar else { return }
        tabBar.isHidden = true
        tabBar.alpha = 0
        tabBar.isUserInteractionEnabled = false
    }

    func refreshImmersiveSystemUI() {
        var controller: UIViewController? = self
        while let current = controller {
            current.setNeedsUpdateOfHomeIndicatorAutoHidden()
            current.setNeedsUpdateOfScreenEdgesDeferringSystemGestures()
            current.setNeedsStatusBarAppearanceUpdate()
            controller = current.parent
        }
        SystemUI.shared.refresh()
    }

    private func setInteractiveContentPopGestureEnabled(_ enabled: Bool) {
        guard disablesInteractiveContentPopGesture else { return }
        if #available(iOS 26.0, *) {
            navigationController?.interactiveContentPopGestureRecognizer?.isEnabled = enabled
        }
    }

    private func setNavigationBarPassthroughEnabled(_ enabled: Bool) {
        guard passesThroughEmptyNavigationBarAreas else { return }
        NuvioNavigationBarPassthrough.setEnabled(enabled, on: navigationController)
    }

    // No frosted strips at the top or bottom of any page. Clears the navigation bar's
    // background on the bar and on this page's navigation item (SwiftUI's own setting
    // alone did not reach it), clears the tab bar's background so only the floating tab
    // pill is left (the "Static" and "Auto-hide" tab bars showed a full-width blurred band
    // behind the pill), and on iOS 26+ hides the blur iOS draws along the top and bottom
    // edges of scrolling content. Buttons, titles and the tab pill itself are untouched.
    private static let transparentBarAppearance: UINavigationBarAppearance = {
        let appearance = UINavigationBarAppearance()
        appearance.configureWithTransparentBackground()
        appearance.backgroundEffect = nil
        appearance.backgroundColor = .clear
        appearance.shadowColor = .clear
        return appearance
    }()

    private static let transparentTabBarAppearance: UITabBarAppearance = {
        let appearance = UITabBarAppearance()
        appearance.configureWithTransparentBackground()
        appearance.backgroundEffect = nil
        appearance.backgroundColor = .clear
        appearance.shadowColor = .clear
        return appearance
    }()

    private func removeBarBlur() {
        var page: UIViewController = self

        if let navigationController {
            let appearance = Self.transparentBarAppearance
            let bar = navigationController.navigationBar
            if bar.standardAppearance !== appearance {
                bar.standardAppearance = appearance
                bar.compactAppearance = appearance
                bar.scrollEdgeAppearance = appearance
                bar.compactScrollEdgeAppearance = appearance
            }

            while let parent = page.parent, parent !== navigationController {
                page = parent
            }
            for item in [navigationItem, page.navigationItem] where item.standardAppearance !== appearance {
                item.standardAppearance = appearance
                item.compactAppearance = appearance
                item.scrollEdgeAppearance = appearance
                item.compactScrollEdgeAppearance = appearance
            }
        }

        if let tabBarController {
            let appearance = Self.transparentTabBarAppearance
            let tabBar = tabBarController.tabBar
            if tabBar.standardAppearance !== appearance {
                tabBar.standardAppearance = appearance
                tabBar.scrollEdgeAppearance = appearance
            }

            // The tab's own item overrides the bar, so clear it on the tab's root controller too.
            var tabPage: UIViewController = self
            while let parent = tabPage.parent, parent !== tabBarController {
                tabPage = parent
            }
            let item = tabPage.tabBarItem
            if let item, item.standardAppearance !== appearance {
                item.standardAppearance = appearance
                item.scrollEdgeAppearance = appearance
            }
        }

        if #available(iOS 26.0, *) {
            Self.hideEdgeEffects(in: page.view)
        }
    }

    @available(iOS 26.0, *)
    private static func hideEdgeEffects(in view: UIView) {
        if let scrollView = view as? UIScrollView {
            if !scrollView.topEdgeEffect.isHidden {
                scrollView.topEdgeEffect.isHidden = true
            }
            if !scrollView.bottomEdgeEffect.isHidden {
                scrollView.bottomEdgeEffect.isHidden = true
            }
        }
        for subview in view.subviews {
            hideEdgeEffects(in: subview)
        }
    }

    private func immersiveController(in controller: UIViewController?) -> UIViewController? {
        guard let controller else { return nil }

        if controller.prefersHomeIndicatorAutoHidden ||
            !controller.preferredScreenEdgesDeferringSystemGestures.isEmpty ||
            controller.prefersStatusBarHidden {
            return controller
        }

        if let presented = immersiveController(in: controller.presentedViewController) {
            return presented
        }

        for child in controller.children.reversed() {
            if let immersiveChild = immersiveController(in: child) {
                return immersiveChild
            }
        }

        return nil
    }

    private var nativeTabsSupported: Bool {
        UIDevice.current.userInterfaceIdiom == .phone &&
            ProcessInfo.processInfo.operatingSystemVersion.majorVersion >= 26
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

// MARK: - Native iOS navigation

@available(iOS 16.0, *)
extension AppRoute {
    var keepsTabBar: Bool { self is SettingsDestinationRoute }
}

extension Array where Element == RouteWrapper {
    var keepsTabBar: Bool { allSatisfy { $0.route.keepsTabBar } }
}

struct RouteWrapper: Hashable, Identifiable {
    let id = UUID()
    let route: AppRoute

    static func == (lhs: RouteWrapper, rhs: RouteWrapper) -> Bool {
        lhs.id == rhs.id
    }

    func hash(into hasher: inout Hasher) {
        hasher.combine(id)
    }
}

@available(iOS 16.0, *)
@MainActor
final class TabNavigationCoordinator: ObservableObject {
    @Published var path: [RouteWrapper] = [] {
        didSet { onPathChanged?(path.isEmpty) }
    }
    var onReturnedToRoot: (() -> Void)?
    var onPathChanged: ((Bool) -> Void)?

    func push(_ route: AppRoute, launchSingleTop: Bool) {
        if launchSingleTop,
           path.last?.route.navigationIdentity == route.navigationIdentity {
            AppKt.disposeRoute(route: route)
            return
        }
        return defaults.bool(forKey: Self.nativeLiveTvEnabledKey)
    }

    private var visibleNativeTabs: [NativeTab] {
        liveTvTabEnabled ? NativeTab.allCases : NativeTab.allCases.filter { $0 != .liveTv }
    }

    private func configureNativeTabBar() {
        tabBar.delegate = self
        tabBar.translatesAutoresizingMaskIntoConstraints = false
        tabBar.items = makeNativeTabItems(for: visibleNativeTabs)
        tabBar.selectedItem = tabBar.items?.first
        applyNativeTabBarAppearance()
        tabBar.alpha = 0
        tabBar.isHidden = true

        view.addSubview(tabBar)
        configureProfileTabTouchOverlay()
        let heightConstraint = tabBar.heightAnchor.constraint(equalToConstant: tabBarHeight)
        tabBarHeightConstraint = heightConstraint
        NSLayoutConstraint.activate([
            tabBar.leadingAnchor.constraint(equalTo: view.leadingAnchor),
            tabBar.trailingAnchor.constraint(equalTo: view.trailingAnchor),
            tabBar.bottomAnchor.constraint(equalTo: view.bottomAnchor),
            heightConstraint,
        ])
    }

    func setPath(_ newPath: [RouteWrapper]) {
        let retainedIDs = Set(newPath.map(\.id))
        let removedRoutes = path
            .filter { !retainedIDs.contains($0.id) }
            .map(\.route)

        let returnedToRoot = !path.isEmpty && newPath.isEmpty
        path = newPath
        removedRoutes
            .filter { !PlayerPictureInPictureParking.shared.isParked($0) }
            .forEach { AppKt.disposeRoute(route: $0) }
        if returnedToRoot {
            onReturnedToRoot?()
        }
    }
}

@available(iOS 16.0, *)
enum NuvioAppTab: String, CaseIterable, Hashable {
    case home = "Home"
    case search = "Search"
    case library = "Library"
    case liveTv = "LiveTv"
    case settings = "Settings"

    var fallbackTitle: String {
        switch self {
        case .liveTv:
            return "Live TV"
        default:
            return String(localized: String.LocalizationValue(rawValue))
        }
    }

    static func from(kotlinName: String?) -> NuvioAppTab? {
        switch kotlinName?.trimmingCharacters(in: .whitespacesAndNewlines).lowercased() {
        case "home": return .home
        case "search": return .search
        case "library": return .library
        case "livetv", "live_tv", "live tv": return .liveTv
        case "settings", "profile": return .settings
        default: return nil
        }
    }

    var iconAssetName: String {
        switch self {
        case .home: return "NuvioTabHome"
        case .search: return "NuvioTabSearch"
        case .library: return "NuvioTabLibrary"
        case .liveTv: return "NuvioTabLiveTv"
        case .settings: return "NuvioTabProfile"
        }
    }

    var fallbackSystemImage: String {
        switch self {
        case .home: return "house.fill"
        case .search: return "magnifyingglass"
        case .library: return "rectangle.stack.fill"
        case .liveTv: return "tv.fill"
        case .settings: return "person.crop.circle.fill"
        }

        let changes = {
            self.tabBar.alpha = visible ? 1 : 0
            self.profileTabTouchOverlay.alpha = visible ? 1 : 0
            self.profileTabAvatarAnimationView.alpha = visible ? 1 : 0
            self.view.layoutIfNeeded()
            self.view.bringSubviewToFront(self.profileTabAvatarAnimationView)
            self.view.bringSubviewToFront(self.profileTabTouchOverlay)
        }

        return (UIImage(systemName: tab.fallbackSystemImage) ?? UIImage())
            .withRenderingMode(.alwaysTemplate)
    }

    static func gradientTinted(_ image: UIImage, colors: [UIColor]) -> UIImage {
        guard colors.count > 1, let mask = image.cgImage else { return image }
        let size = image.size
        guard size.width > 0, size.height > 0 else { return image }
        let cgColors = colors.map { $0.cgColor } as CFArray
        guard let gradient = CGGradient(
            colorsSpace: CGColorSpaceCreateDeviceRGB(),
            colors: cgColors,
            locations: nil
        ) else {
            return image
        }

        return UIGraphicsImageRenderer(size: size).image { context in
            let cgContext = context.cgContext
            let rect = CGRect(origin: .zero, size: size)
            cgContext.translateBy(x: 0, y: size.height)
            cgContext.scaleBy(x: 1, y: -1)
            cgContext.clip(to: rect, mask: mask)
            cgContext.drawLinearGradient(
                gradient,
                start: .zero,
                end: CGPoint(x: size.width, y: size.height),
                options: [.drawsBeforeStartLocation, .drawsAfterEndLocation]
            )
        }.withRenderingMode(.alwaysOriginal)
    }

    static func profileAvatar(
        name: String?,
        avatarColor: UIColor?,
        backgroundColor: UIColor?,
        avatarImage: UIImage?,
        selected: Bool,
        accent: UIColor
    ) -> UIImage {
        guard name != nil || avatarColor != nil || avatarImage != nil else {
            return image(for: .settings)
        }

        if animated && view.window != nil {
            UIView.animate(
                withDuration: 0.22,
                delay: 0,
                options: [.beginFromCurrentState, .curveEaseInOut],
                animations: changes,
                completion: completion
            )
        )
    }
}

@available(iOS 16.0, *)
final class NativeTabIconStore: ObservableObject {
    private static let chromeDidChange = Notification.Name("NuvioNativeTabChromeDidChange")
    private static let accentKey = "NuvioNativeTabAccentColor"
    private static let accentGradientKey = "NuvioNativeTabAccentGradient"
    private static let profileNameKey = "NuvioNativeProfileName"
    private static let profileColorKey = "NuvioNativeProfileAvatarColor"
    private static let profileURLKey = "NuvioNativeProfileAvatarURL"
    private static let profileBackgroundKey = "NuvioNativeProfileAvatarBackgroundColor"

    @Published private(set) var revision = 0
    @Published private(set) var accentColor = UIColor(
        red: 0.96,
        green: 0.96,
        blue: 0.96,
        alpha: 1
    )

    @Published private(set) var accentColors: [UIColor] = []

    func accentStyle(opacity: CGFloat = 1) -> AnyShapeStyle {
        let colors = accentColors.isEmpty ? [accentColor] : accentColors
        let faded = colors.map { Color(uiColor: $0).opacity(opacity) }
        guard faded.count > 1 else {
            return AnyShapeStyle(faded[0])
        }
        return AnyShapeStyle(
            LinearGradient(colors: faded, startPoint: .topLeading, endPoint: .bottomTrailing)
        )
    }

    private var observer: NSObjectProtocol?
    private var profileAvatarURL: String?
    private var profileAvatarImage: UIImage?
    private var profileAvatarTask: URLSessionDataTask?

    init() {
        UITabBar.appearance().unselectedItemTintColor = UIColor(
            red: 150 / 255,
            green: 156 / 255,
            blue: 163 / 255,
            alpha: 1
        )
        observer = NotificationCenter.default.addObserver(
            forName: Self.chromeDidChange,
            object: nil,
            queue: .main
        ) { [weak self] _ in
            self?.reload()
        }
    }

    private func syncSelectedNativeTab() {
        tabBar.selectedItem = tabBar.items?.first(where: { $0.tag == currentNativeSelectedTab.tag })
        updateProfileTabAvatarAnimation()
    }

    func image(for tab: NuvioAppTab, selected: Bool) -> UIImage {
        guard tab == .settings else {
            let icon = NuvioNativeTabIcon.image(for: tab)
            guard selected, accentColors.count > 1 else { return icon }
            return NuvioNativeTabIcon.gradientTinted(icon, colors: accentColors)
        }
    }

    private func reload() {
        let defaults = UserDefaults.standard
        accentColor = UIColor(hexString: defaults.string(forKey: Self.accentKey))
            ?? UIColor(red: 0.96, green: 0.96, blue: 0.96, alpha: 1)
        accentColors = (defaults.string(forKey: Self.accentGradientKey) ?? "")
            .split(separator: ",")
            .compactMap { UIColor(hexString: String($0).trimmingCharacters(in: .whitespaces)) }

        let nextURL = defaults.string(forKey: Self.profileURLKey)
        guard nextURL != profileAvatarURL else {
            revision &+= 1
            return
        }

        profileAvatarTask?.cancel()
        profileAvatarTask = nil
        profileAvatarURL = nextURL
        profileAvatarImage = nil
        revision &+= 1

        tabBar.items = makeNativeTabItems(for: tabs)
        let rawSelectedTab = UserDefaults.standard.string(forKey: Self.nativeSelectedTabKey)
        if rawSelectedTab == NativeTab.liveTv.rawValue && !liveTvTabEnabled {
            UserDefaults.standard.set(NativeTab.home.rawValue, forKey: Self.nativeSelectedTabKey)
            DispatchQueue.main.async {
                NativeTabBridgeKt.nativeTabSelect(tabName: NativeTab.home.rawValue)
            }
        }
        updateProfileTabTouchOverlayFrame()
    }

@available(iOS 16.0, *)
@MainActor
final class NativeProfileTabInteractionCoordinator: NSObject, UIGestureRecognizerDelegate {
    var onLongPress: (() -> Void)?
    private(set) var isHandlingLongPress = false
    private(set) var suppressesProfileSelection = false
    func setSuppressesProfileSelection(_ enabled: Bool) {
        suppressesProfileSelection = enabled
    }

    private weak var tabBarController: UITabBarController?
    private var selectedIndexBeforeLongPress: Int?
    private let competingRecognizers = NSHashTable<UIGestureRecognizer>.weakObjects()
    private lazy var recognizer: UILongPressGestureRecognizer = {
        let recognizer = UILongPressGestureRecognizer(
            target: self,
            action: #selector(handleLongPress(_:))
        )
        recognizer.minimumPressDuration = 0.45
        recognizer.cancelsTouchesInView = true
        recognizer.delegate = self
        return recognizer
    }()

    func attach(to tabBarController: UITabBarController) {
        guard self.tabBarController !== tabBarController else { return }
        self.tabBarController?.tabBar.removeGestureRecognizer(recognizer)
        tabBarController.tabBar.addGestureRecognizer(recognizer)
        self.tabBarController = tabBarController
        publishIconFrame()
        onAttached?()
    }

    var onAttached: (() -> Void)?

    func measureNativeTabBarMetrics() -> NuvioTabBarMetrics? {
        guard let tabBar = tabBarController?.tabBar,
              let window = tabBar.window,
              !tabBar.isHidden,
              tabBar.bounds.width > 0 else { return nil }
        let platterFrame: CGRect
        if let platter = Self.findPlatter(in: tabBar) {
            platterFrame = platter.convert(platter.bounds, to: window)
        } else if #available(iOS 17.0, *),
                  let items = tabBar.items, !items.isEmpty {
            let union = items
                .compactMap { $0.frame(in: tabBar) }
                .reduce(CGRect.null) { $0.union($1) }
            guard !union.isNull else { return nil }
            platterFrame = tabBar.convert(union.insetBy(dx: -4, dy: -4), to: window)
        } else {
            return nil
        }
        guard platterFrame.width > 0, platterFrame.height > 0 else { return nil }
        let (items, selectionFrame, selectionIndex) = Self.measureItems(
            in: tabBar,
            window: window,
            platterFrame: platterFrame
        )
        return NuvioTabBarMetrics(
            windowSize: window.bounds.size,
            leadingInset: platterFrame.minX,
            trailingInset: window.bounds.width - platterFrame.maxX,
            bottomInset: window.bounds.height - platterFrame.maxY,
            height: platterFrame.height,
            items: items,
            selectionFrame: selectionFrame,
            selectionItemIndex: selectionFrame == nil ? nil : selectionIndex
        )
    }

    private static func measureItems(
        in tabBar: UITabBar,
        window: UIWindow,
        platterFrame: CGRect
    ) -> ([NuvioTabBarItemMetrics], CGRect?, Int?) {
        func local(_ view: UIView) -> CGRect {
            view.convert(view.bounds, to: window).offsetBy(dx: -platterFrame.minX, dy: -platterFrame.minY)
        }
        func descendants(of view: UIView) -> [UIView] {
            view.subviews.flatMap { [$0] + descendants(of: $0) }
        }

        let buttons = descendants(of: tabBar)
            .filter { view in
                String(describing: type(of: view)).localizedCaseInsensitiveContains("TabBarButton") &&
                    !view.isHidden && view.alpha > 0.01 && view.bounds.width > 0
            }
            .sorted { local($0).minX < local($1).minX }
        guard !buttons.isEmpty else { return ([], nil, nil) }

        var items: [NuvioTabBarItemMetrics] = []
        for button in buttons {
            let inner = descendants(of: button).filter { !$0.isHidden && $0.alpha > 0.01 }
            guard let imageView = inner
                .compactMap({ $0 as? UIImageView })
                .filter({ $0.image != nil })
                .max(by: { $0.bounds.width * $0.bounds.height < $1.bounds.width * $1.bounds.height })
            else { return ([], nil, nil) }
            let label = inner.compactMap { $0 as? UILabel }.first { !($0.text ?? "").isEmpty }
            items.append(
                NuvioTabBarItemMetrics(
                    buttonFrame: local(button),
                    iconFrame: local(imageView),
                    labelFrame: label.map(local),
                    labelFont: label?.font
                )
            )
        }

        var selectionFrame: CGRect?
        var selectionIndex: Int?
        if let selectedItem = tabBar.selectedItem,
           let selectedIndex = tabBar.items?.firstIndex(of: selectedItem),
           selectedIndex < buttons.count {
            selectionIndex = selectedIndex
            let buttonFrame = local(buttons[selectedIndex])
            let center = CGPoint(x: buttonFrame.midX, y: buttonFrame.midY)
            let buttonSet = Set(buttons.flatMap { [ObjectIdentifier($0)] + descendants(of: $0).map(ObjectIdentifier.init) })
            selectionFrame = descendants(of: tabBar)
                .filter { view in
                    guard !buttonSet.contains(ObjectIdentifier(view)),
                          !view.isHidden, view.alpha > 0.01 else { return false }
                    let frame = local(view)
                    return frame.contains(center) &&
                        frame.width >= buttonFrame.width * 0.8 &&
                        frame.width <= buttonFrame.width * 1.8 &&
                        frame.height >= platterFrame.height * 0.6 &&
                        frame.height < platterFrame.height + 1
                }
                .map(local)
                .min { $0.width * $0.height < $1.width * $1.height }
        }
        return (items, selectionFrame, selectionIndex)
    }

    private static func findPlatter(in view: UIView) -> UIView? {
        var best: UIView?
        var stack = view.subviews
        while let current = stack.popLast() {
            let name = String(describing: type(of: current))
            if name.localizedCaseInsensitiveContains("Platter"),
               !current.isHidden,
               current.bounds.width > (best?.bounds.width ?? 0) {
                best = current
            }
            stack.append(contentsOf: current.subviews)
        }
        return best
    }

    func publishIconFrame() {
        guard #available(iOS 17.0, *),
              let tabBar = tabBarController?.tabBar,
              let profileItem = tabBar.items?.last,
              let itemFrame = profileItem.frame(in: tabBar) else { return }
        let windowFrame = tabBar.convert(itemFrame, to: nil)
        NativeTabBridgeKt.publishProfileTabIconFrame(
            xDp: Float(windowFrame.origin.x),
            yDp: Float(windowFrame.origin.y),
            widthDp: Float(windowFrame.width),
            heightDp: Float(windowFrame.height)
        )
    }

    func gestureRecognizer(
        _ gestureRecognizer: UIGestureRecognizer,
        shouldReceive touch: UITouch
    ) -> Bool {
        guard gestureRecognizer === recognizer,
              let tabBar = tabBarController?.tabBar,
              let profileItem = tabBar.items?.last else {
            return false
        }
        competingRecognizers.removeAllObjects()
        guard #available(iOS 17.0, *),
              let profileFrame = profileItem.frame(in: tabBar) else { return false }
        return profileFrame.contains(touch.location(in: tabBar))
    }

    func gestureRecognizer(
        _ gestureRecognizer: UIGestureRecognizer,
        shouldRecognizeSimultaneouslyWith otherGestureRecognizer: UIGestureRecognizer
    ) -> Bool {
        guard gestureRecognizer === recognizer || otherGestureRecognizer === recognizer else {
            return false
        }
        competingRecognizers.add(
            gestureRecognizer === recognizer ? otherGestureRecognizer : gestureRecognizer
        )
        return true
    }

    @objc private func handleLongPress(_ recognizer: UILongPressGestureRecognizer) {
        switch recognizer.state {
        case .began:
            selectedIndexBeforeLongPress = tabBarController?.selectedIndex
            isHandlingLongPress = true
            suppressesProfileSelection = true
            UIImpactFeedbackGenerator(style: .medium).impactOccurred()
            onLongPress?()
            competingRecognizers.allObjects.forEach { competingRecognizer in
                competingRecognizer.isEnabled = false
                competingRecognizer.isEnabled = true
            }
            if let selectedIndexBeforeLongPress {
                tabBarController?.selectedIndex = selectedIndexBeforeLongPress
            }
        case .ended, .cancelled, .failed:
            let selectedIndex = selectedIndexBeforeLongPress
            isHandlingLongPress = false
            DispatchQueue.main.async { [weak self] in
                guard let self else { return }
                if let selectedIndex {
                    self.tabBarController?.selectedIndex = selectedIndex
                }
                self.selectedIndexBeforeLongPress = nil
                self.suppressesProfileSelection = false
            }
        default:
            break
        }
    }
}

@available(iOS 16.0, *)
@MainActor
final class AppNavigationCoordinator: ObservableObject {
    private static let nativeTabBarVisibleKey = "NuvioNativeTabBarVisible"
    private static let liveTvTabVisibleKey = "NuvioLiveTvNavigationVisible"

    @Published var selectedTab: NuvioAppTab = .home {
        didSet {
            if selectedTab != oldValue {
                setTabBarVisible(true)
                refreshSelectedTabDepth()
                NativeTabBridgeKt.nativeTabVisibilityChanged(tabName: selectedTab.rawValue)
            }
        }
    }
    @Published private(set) var isMainContentMounted = false
    @Published private(set) var isMainContentVisible = false
    @Published private(set) var isAppReady = false
    @Published private(set) var isTabBarVisible = true
    @Published private(set) var isNativeTabBarVisible = true
    @Published private(set) var isCompactPillHidden = true
    @Published private(set) var nativeTabBarMetricsBySize: [String: NuvioTabBarMetrics] = [:]

    func nativeTabBarMetrics(for size: CGSize) -> NuvioTabBarMetrics? {
        nativeTabBarMetricsBySize[NuvioTabBarMetrics.key(for: size)]
    }

    func refreshNativeTabBarMetrics() {
        guard isNativeTabBarVisible,
              let metrics = profileTabInteraction.measureNativeTabBarMetrics() else { return }
        let key = NuvioTabBarMetrics.key(for: metrics.windowSize)
        guard nativeTabBarMetricsBySize[key] != metrics else { return }
        nativeTabBarMetricsBySize[key] = metrics
    }
    @Published private(set) var tabBarBehavior: NuvioTabBarBehavior = NuvioTabBarBehavior.current()
    @Published private(set) var isSelectedTabAtRoot = true
    @Published private(set) var isLiveTvTabVisible = false
    @Published private var localizedTabTitles: [NuvioAppTab: String] = [:]
    @Published private(set) var localizedSwitchProfileTitle = ""
    @Published private(set) var localizedAddProfileTitle = ""
    @Published var isProfileSwitcherPresented = false

    private var tabBarTransitionTask: Task<Void, Never>?

    let homeCoordinator = TabNavigationCoordinator()
    let searchCoordinator = TabNavigationCoordinator()
    let libraryCoordinator = TabNavigationCoordinator()
    let liveTvCoordinator = TabNavigationCoordinator()
    let settingsCoordinator = TabNavigationCoordinator()
    let appGateController = AppGateController()
    let profileSwitcherController = NativeProfileSwitcherController()
    let profileTabInteraction = NativeProfileTabInteractionCoordinator()

    init() {
        PlayerPictureInPictureParking.shared.appCoordinator = self
        setTabBarVisible(true)
        reloadTabBarBehavior()
        reloadLiveTvTabVisibility()
        NativeTabBridgeKt.observeNativePopToRoot { [weak self] tabName in
            guard let self, let tab = NuvioAppTab.from(kotlinName: tabName) else { return }
            self.coordinator(for: tab).popToRoot()
            self.selectedTab = tab
        }
        NativeTabBridgeKt.observeNativeTabBarVisible { [weak self] visible in
            guard let self else { return }
            self.setTabBarVisible(visible.boolValue, animated: self.tabBarBehavior == .morphed)
        }
        allCoordinators.forEach { coordinator in
            coordinator.onReturnedToRoot = { [weak self] in
                self?.setTabBarVisible(true)
            }
            coordinator.onPathChanged = { [weak self] _ in
                self?.refreshSelectedTabDepth()
            }
        }
        profileTabInteraction.onAttached = { [weak self] in
            DispatchQueue.main.async { self?.refreshNativeTabBarMetrics() }
        }
        profileTabInteraction.onLongPress = { [weak self] in
            guard let self, self.isAppReady else { return }
            self.isProfileSwitcherPresented = true
        }
    }

    private var allCoordinators: [TabNavigationCoordinator] {
        [homeCoordinator, searchCoordinator, libraryCoordinator, liveTvCoordinator, settingsCoordinator]
    }

    var availableTabs: [NuvioAppTab] {
        NuvioAppTab.allCases.filter { $0 != .liveTv || isLiveTvTabVisible }
    }

    var usesGlassTabBarOverlay: Bool {
        tabBarBehavior.usesCompactPill ||
            (tabBarBehavior.isEnabled && UIDevice.current.userInterfaceIdiom == .pad)
    }

    func reloadTabBarBehavior() {
        let behavior = NuvioTabBarBehavior.current()
        guard tabBarBehavior != behavior else { return }
        tabBarBehavior = behavior
        if !behavior.respondsToScroll {
            setTabBarVisible(true)
        }
    }

    private func refreshSelectedTabDepth() {
        let atRoot = coordinator(for: selectedTab).path.keepsTabBar
        if isSelectedTabAtRoot != atRoot {
            isSelectedTabAtRoot = atRoot
        }
    }

    func requestTabBarVisible(_ visible: Bool) {
        setTabBarVisible(visible, animated: tabBarBehavior == .morphed)
    }

    func reloadTabBarVisibility() {
        guard tabBarBehavior.respondsToScroll else {
            setTabBarVisible(true)
            return
        }
        guard let visible = UserDefaults.standard.object(
            forKey: Self.nativeTabBarVisibleKey
        ) as? Bool else {
            return
        }
        setTabBarVisible(visible, animated: tabBarBehavior == .morphed)
    }

    private func setTabBarVisible(_ visible: Bool, animated: Bool = false) {
        UserDefaults.standard.set(visible, forKey: Self.nativeTabBarVisibleKey)
        if visible {
            profileTabInteraction.publishIconFrame()
        }

        tabBarTransitionTask?.cancel()
        tabBarTransitionTask = nil

        guard animated else {
            isTabBarVisible = visible
            isNativeTabBarVisible = visible
            isCompactPillHidden = visible
            return
        }

        isNativeTabBarVisible = false
        isCompactPillHidden = false
        guard isTabBarVisible != visible else { return }
        isTabBarVisible = visible
    }

    func reloadLiveTvTabVisibility() {
        let visible = UserDefaults.standard.bool(forKey: Self.liveTvTabVisibleKey)
        guard isLiveTvTabVisible != visible else { return }
        if !visible && selectedTab == .liveTv {
            selectedTab = .home
        }
        isLiveTvTabVisible = visible
    }

    func coordinator(for tab: NuvioAppTab) -> TabNavigationCoordinator {
        switch tab {
        case .home: return homeCoordinator
        case .search: return searchCoordinator
        case .library: return libraryCoordinator
        case .liveTv: return liveTvCoordinator
        case .settings: return settingsCoordinator
        }
    }

    func activateTab(named tabName: String) {
        guard let tab = NuvioAppTab.from(kotlinName: tabName) else { return }
        guard tab != .liveTv || isLiveTvTabVisible else {
            selectedTab = .home
            return
        }
        if tab == .home || isAppReady {
            selectedTab = tab
        }
    }

    @objc private func handleNativeProfileTabTap() {
        if profileLongPressHandled {
            profileLongPressHandled = false
            restoreProfileTabTouchIfNeeded()
            return
        }
        profileTouchRestoreTab = nil
        selectNativeTab(.settings)
    }

    func updateTabTitles(
        home: String,
        search: String,
        library: String,
        profile: String,
        switchProfile: String,
        addProfile: String
    ) {
        localizedTabTitles = [
            .home: home,
            .search: search,
            .library: library,
            .liveTv: NuvioAppTab.liveTv.fallbackTitle,
            .settings: profile,
        ]
        localizedSwitchProfileTitle = switchProfile
        localizedAddProfileTitle = addProfile
    }

    func updateAppReady(_ ready: Bool) {
        isAppReady = ready
        if !ready {
            isProfileSwitcherPresented = false
            setTabBarVisible(true)
            selectedTab = .home
            allCoordinators.forEach { $0.popToRoot() }
        }
        return tab
    }

    private func selectNativeTab(_ tab: NativeTab) {
        guard visibleNativeTabs.contains(tab) else { return }
        tabBar.selectedItem = tabBar.items?.first(where: { $0.tag == tab.tag })
        UserDefaults.standard.set(tab.rawValue, forKey: Self.nativeSelectedTabKey)
        updateProfileTabAvatarAnimation()
        NativeTabBridgeKt.nativeTabSelect(tabName: tab.rawValue)
    }

    private func configureProfileTabTouchOverlay() {
        profileTabAvatarAnimationView.contentMode = .scaleAspectFill
        profileTabAvatarAnimationView.clipsToBounds = false
        profileTabAvatarAnimationView.isUserInteractionEnabled = false
        profileTabAvatarAnimationView.alpha = 0
        profileTabAvatarAnimationView.isHidden = true
        profileTabAvatarAnimationView.layer.zPosition = 1000
        view.addSubview(profileTabAvatarAnimationView)

        profileTabTouchOverlay.backgroundColor = .clear
        profileTabTouchOverlay.isOpaque = false
        profileTabTouchOverlay.isExclusiveTouch = true
        profileTabTouchOverlay.accessibilityLabel = NativeTab.settings.localizedTitle()
        profileTabTouchOverlay.accessibilityTraits = .button
        profileTabTouchOverlay.addTarget(
            self,
            action: #selector(handleNativeProfileTabTouchDown),
            for: .touchDown
        )
        profileTabTouchOverlay.addTarget(
            self,
            action: #selector(handleNativeProfileTabTap),
            for: .touchUpInside
        )
        profileTabTouchOverlay.addTarget(
            self,
            action: #selector(handleNativeProfileTabTouchCancel),
            for: [.touchCancel, .touchUpOutside]
        )

    func setMainContentMounted(_ mounted: Bool) {
        isMainContentMounted = mounted
        if !mounted {
            isMainContentVisible = false
            selectedTab = .home
        }
    }

    func setMainContentVisible(_ visible: Bool) {
        isMainContentVisible = visible
    }

    func openProfileManagement() {
        isProfileSwitcherPresented = false
        profileSwitcherController.requestManageProfiles()
    }

        profileTabTouchOverlay.alpha = 0
        profileTabTouchOverlay.isHidden = true
        view.addSubview(profileTabTouchOverlay)
        updateProfileTabTouchOverlayFrame()
    }

    func push(
        _ route: AppRoute,
        from origin: TabNavigationCoordinator,
        launchSingleTop: Bool
    ) {
        guard isAppReady else {
            AppKt.disposeRoute(route: route)
            return
        }
        if route is PlayerRoute, !PlayerPictureInPictureParking.shared.isParked(route) {
            PlayerPictureInPictureParking.shared.closeParked()
        }
        let requestedTab = NuvioAppTab.from(kotlinName: route.preferredTabName)
            ?? tab(for: origin)
            ?? selectedTab
        let targetTab = requestedTab == .liveTv && !isLiveTvTabVisible ? .home : requestedTab
        let target = coordinator(for: targetTab)
        selectedTab = targetTab
        target.push(route, launchSingleTop: launchSingleTop)
    }

    func showParkedPlayerRoute(_ route: AppRoute, in target: TabNavigationCoordinator) {
        if let targetTab = tab(for: target) {
            selectedTab = targetTab
        }
        target.path.append(RouteWrapper(route: route))
    }

    func replace(_ route: AppRoute, in target: TabNavigationCoordinator) {
        guard isAppReady else {
            AppKt.disposeRoute(route: route)
            return
        }
        guard let settingsIndex = tabs.firstIndex(of: .settings) else {
            profileTabTouchOverlay.frame = .zero
            updateProfileTabAvatarAnimationFrame()
            syncProfileTabAvatarAnimationVisibility()
            return
        }

@available(iOS 16.0, *)
struct NativeNavComposeView: UIViewControllerRepresentable {
    let tab: NuvioAppTab
    let usesNativeTabBar: Bool
    let usesTabletFloatingTabBar: Bool
    let coordinator: TabNavigationCoordinator
    let appCoordinator: AppNavigationCoordinator

    func makeUIViewController(context: Context) -> UIViewController {
        let verticalBar = VerticalBar()
        let controller = MainViewControllerKt.MainViewController(
            initialTabName: tab.rawValue,
            useNativeTabBar: usesNativeTabBar,
            useTabletFloatingTabBar: usesTabletFloatingTabBar,
            onNavigate: { route, launchSingleTop in
                appCoordinator.push(
                    route,
                    from: coordinator,
                    launchSingleTop: launchSingleTop.boolValue
                )
            },
            onGoBack: {
                coordinator.pop()
            },
            onReplace: { route in
                appCoordinator.replace(route, in: coordinator)
            },
            onActivate: { tabName in
                appCoordinator.activateTab(named: tabName)
            },
            onTabTitles: { home, search, library, profile, switchProfile, addProfile in
                appCoordinator.updateTabTitles(
                    home: home,
                    search: search,
                    library: library,
                    profile: profile,
                    switchProfile: switchProfile,
                    addProfile: addProfile
                )
            },
            appGateController: appCoordinator.appGateController,
            verticalBar: verticalBar
        )
        return NuvioComposeHost.wrap(
            controller,
            hidesContainingTabBar: !usesNativeTabBar,
            verticalBar: verticalBar,
            onTabBarControllerAvailable: { tabBarController in
                if usesNativeTabBar {
                    appCoordinator.profileTabInteraction.attach(to: tabBarController)
                }
            }
        )
        profileTabTouchOverlay.frame = tabBar.convert(overlayFrameInTabBar, to: view)
        profileTabTouchOverlay.alpha = tabBar.alpha
        updateProfileTabAvatarAnimationFrame()
        syncProfileTabAvatarAnimationVisibility()
        view.bringSubviewToFront(profileTabAvatarAnimationView)
        view.bringSubviewToFront(profileTabTouchOverlay)
    }

    private var nativeTabAccentColor: UIColor {
        UIColor(hexString: UserDefaults.standard.string(forKey: Self.nativeTabAccentColorKey)) ??
            UIColor(red: 0.96, green: 0.96, blue: 0.96, alpha: 1)
    }

    private func applyNativeTabBarAppearance() {
        let accent = nativeTabAccentColor
        let unselected = UIColor(red: 150 / 255, green: 156 / 255, blue: 163 / 255, alpha: 1)

        updateNativeTabTitles()
        refreshProfileAvatarImageIfNeeded()
        updateNativeTabImages(accent: accent)
        updateProfileTabAvatarAnimation()

        tabBar.tintColor = accent
        tabBar.unselectedItemTintColor = unselected

        let appearance = tabBar.standardAppearance.copy() as! UITabBarAppearance
        appearance.stackedLayoutAppearance.normal.iconColor = unselected
        appearance.stackedLayoutAppearance.normal.titleTextAttributes = [.foregroundColor: unselected]
        appearance.stackedLayoutAppearance.selected.iconColor = accent
        appearance.stackedLayoutAppearance.selected.titleTextAttributes = [.foregroundColor: accent]
        appearance.inlineLayoutAppearance.normal.iconColor = unselected
        appearance.inlineLayoutAppearance.normal.titleTextAttributes = [.foregroundColor: unselected]
        appearance.inlineLayoutAppearance.selected.iconColor = accent
        appearance.inlineLayoutAppearance.selected.titleTextAttributes = [.foregroundColor: accent]
        appearance.compactInlineLayoutAppearance.normal.iconColor = unselected
        appearance.compactInlineLayoutAppearance.normal.titleTextAttributes = [.foregroundColor: unselected]
        appearance.compactInlineLayoutAppearance.selected.iconColor = accent
        appearance.compactInlineLayoutAppearance.selected.titleTextAttributes = [.foregroundColor: accent]
        tabBar.standardAppearance = appearance
        tabBar.scrollEdgeAppearance = appearance
    }

    private func updateNativeTabImages(accent: UIColor) {
        tabBar.items?.forEach { item in
            guard let tab = NativeTab(tag: item.tag) else { return }
            item.image = nativeTabImage(for: tab, selected: false, accent: accent)
            item.selectedImage = nativeTabImage(for: tab, selected: true, accent: accent)
        }
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
        (uiViewController as? RootComposeViewController)?.refreshContainingTabBarVisibility()
    }
}

@available(iOS 16.0, *)
struct AppGateComposeView: UIViewControllerRepresentable {
    let appCoordinator: AppNavigationCoordinator

    func makeUIViewController(context: Context) -> UIViewController {
        let verticalBar = VerticalBar()
        let controller = MainViewControllerKt.AppGateViewController(
            appGateController: appCoordinator.appGateController,
            nativeProfileSwitcherController: appCoordinator.profileSwitcherController,
            onActivate: { tabName in
                appCoordinator.activateTab(named: tabName)
            },
            onAppReady: { ready in
                appCoordinator.updateAppReady(ready.boolValue)
            },
            onMainContentMountChanged: { mounted in
                appCoordinator.setMainContentMounted(mounted.boolValue)
            },
            onMainContentVisibleChanged: { visible in
                appCoordinator.setMainContentVisible(visible.boolValue)
            },
            verticalBar: verticalBar
        )
        controller.view.isOpaque = false
        return NuvioComposeHost.wrap(controller, backgroundColor: .clear, verticalBar: verticalBar)
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

@available(iOS 16.0, *)
struct DetailComposeView: UIViewControllerRepresentable {
    let route: AppRoute
    let usesNativeTabBar: Bool
    let usesTabletFloatingTabBar: Bool
    let coordinator: TabNavigationCoordinator
    let appCoordinator: AppNavigationCoordinator

    func makeUIViewController(context: Context) -> UIViewController {
        if route is PlayerRoute {
            return PlayerRouteContainerViewController(
                route: route,
                coordinator: coordinator,
                makeContent: { makeScreenController() }
            )
        }
        return makeScreenController()
    }

    private func makeScreenController() -> UIViewController {
        let route = route
        let coordinator = coordinator
        let appCoordinator = appCoordinator
        let parking = PlayerPictureInPictureParking.shared
        let verticalBar = VerticalBar()
        let hinge = Hinge()
        let controller = MainViewControllerKt.ScreenViewController(
            route: route,
            onNavigate: { newRoute, launchSingleTop in
                appCoordinator.push(
                    newRoute,
                    from: coordinator,
                    launchSingleTop: launchSingleTop.boolValue
                )
            },
            onGoBack: {
                if parking.isParked(route) {
                    parking.closeParked()
                } else {
                    coordinator.pop()
                }
            },
            onReplace: { newRoute in
                if parking.isParked(route) {
                    AppKt.disposeRoute(route: newRoute)
                    return
                }
                appCoordinator.replace(newRoute, in: coordinator)
            },
            onActivate: { tabName in
                appCoordinator.activateTab(named: tabName)
            },
            appGateController: appCoordinator.appGateController,
            useNativeTabBar: usesNativeTabBar,
            useTabletFloatingTabBar: usesTabletFloatingTabBar,
            verticalBar: verticalBar,
            hinge: hinge
        )
        return NuvioComposeHost.wrap(
            controller,
            disablesInteractiveContentPopGesture: true,
            passesThroughEmptyNavigationBarAreas: true,
            verticalBar: verticalBar,
            hinge: route is PlayerRoute ? hinge : nil
        )
    }

    private func updateProfileTabAvatarAnimation() {
        let defaults = UserDefaults.standard
        let selected = currentNativeSelectedTab == .settings
        let accent = nativeTabAccentColor
        let animationKey = [
            profileAvatarImageURL ?? "",
            defaults.string(forKey: Self.nativeProfileNameKey) ?? "",
            defaults.string(forKey: Self.nativeProfileAvatarColorKey) ?? "",
            defaults.string(forKey: Self.nativeProfileAvatarBackgroundColorKey) ?? "",
            defaults.string(forKey: Self.nativeTabAccentColorKey) ?? "",
            selected ? "selected" : "normal",
        ].joined(separator: "|")

        guard let avatarAnimation = NuvioNativeTabIcon.profileAvatarAnimation(
            name: defaults.string(forKey: Self.nativeProfileNameKey),
            avatarColor: UIColor(hexString: defaults.string(forKey: Self.nativeProfileAvatarColorKey)),
            backgroundColor: UIColor(hexString: defaults.string(forKey: Self.nativeProfileAvatarBackgroundColorKey)),
            avatarFrames: profileAvatarAnimationSourceFrames,
            avatarDuration: profileAvatarAnimationSourceDuration,
            selected: selected,
            accent: accent
        ) else {
            clearProfileTabAvatarAnimation()
            return
        }

        if profileTabAvatarAnimationKey != animationKey {
            stopProfileTabAvatarAnimationTimer()
            profileTabAvatarAnimationFrames = avatarAnimation.frames
            profileTabAvatarAnimationFrameIndex = 0
            profileTabAvatarAnimationFrameDelay = max(
                avatarAnimation.duration / Double(max(avatarAnimation.frames.count, 1)),
                0.02
            )
            .ignoresSafeArea(.all)
            .navigationTitle(appCoordinator.title(for: tab))
            .navigationBarHidden(true)
            .navigationDestination(for: RouteWrapper.self) { wrapper in
                if appCoordinator.selectedTab == tab {
                    DetailDestinationView(
                        wrapper: wrapper,
                        usesNativeTabBar: usesNativeTabBar,
                        usesTabletFloatingTabBar: usesTabletFloatingTabBar,
                        coordinator: coordinator,
                        appCoordinator: appCoordinator
                    )
                    .id(wrapper.id)
                } else {
                    Color.clear
                }
            }
            profileTabAvatarAnimationKey = animationKey
        }
        .toolbar(
            usesNativeTabBar &&
                appCoordinator.isMainContentVisible &&
                coordinator.path.keepsTabBar &&
                !appCoordinator.usesGlassTabBarOverlay &&
                appCoordinator.isNativeTabBarVisible
                ? Visibility.visible
                : Visibility.hidden,
            for: .tabBar
        )
        .animation(
            appCoordinator.tabBarBehavior == .autoHide
                ? .easeInOut(duration: 0.18)
                : nil,
            value: appCoordinator.isNativeTabBarVisible
        )
    }
}

@available(iOS 16.0, *)
private struct NativeToolbarReadabilityFade: View {
    var body: some View {
        Rectangle()
            .fill(
                LinearGradient(
                    stops: [
                        .init(color: Color(uiColor: nuvioBackgroundColor), location: 0),
                        .init(color: Color(uiColor: nuvioBackgroundColor).opacity(0.78), location: 0.55),
                        .init(color: Color(uiColor: nuvioBackgroundColor).opacity(0), location: 1),
                    ],
                    startPoint: .top,
                    endPoint: .bottom
                )
            )
            .frame(height: 120)
            .ignoresSafeArea(edges: [.top, .leading, .trailing])
            .allowsHitTesting(false)
            .accessibilityHidden(true)
    }
}

@available(iOS 16.0, *)
private struct DetailDestinationView: View {
    let wrapper: RouteWrapper
    let usesNativeTabBar: Bool
    let usesTabletFloatingTabBar: Bool
    @ObservedObject var coordinator: TabNavigationCoordinator
    @ObservedObject var appCoordinator: AppNavigationCoordinator

    private var usesComposeNavigationHeader: Bool {
        wrapper.route is DetailRoute || wrapper.route is StreamRoute
    }

    private var respectsNativeNavigationSafeArea: Bool {
        false
    }

    private var showsReadabilityFade: Bool {
        !wrapper.route.hidesNavigationBar && !usesComposeNavigationHeader
    }

    private var content: some View {
        ZStack(alignment: .top) {
            DetailComposeView(
                route: wrapper.route,
                usesNativeTabBar: usesNativeTabBar,
                usesTabletFloatingTabBar: usesTabletFloatingTabBar,
                coordinator: coordinator,
                appCoordinator: appCoordinator
            )
            .ignoresSafeArea(
                .all,
                edges: respectsNativeNavigationSafeArea ? [.horizontal, .bottom] : .all
            )

        let side = min(max(max(imageFrame.width, imageFrame.height), 24), 34)
        profileTabAvatarAnimationView.frame = CGRect(
            x: imageFrame.midX - side / 2,
            y: imageFrame.midY - side / 2,
            width: side,
            height: side
        ).integral
        profileTabAvatarAnimationView.layer.cornerRadius = side / 2
        profileTabAvatarAnimationView.layer.masksToBounds = false
        profileTabAvatarAnimationView.isHidden = false
    }

    private func profileTabNativeImageFrameInView() -> CGRect? {
        guard let profileTabButton = nativeTabButton(for: .settings) else {
            profileTabStaticImageView?.alpha = 1
            profileTabStaticImageView = nil
            return nil
        }
        let fallbackFrame = profileTabFallbackAvatarFrame(in: profileTabButton)

        let imageViews = profileTabButton.recursiveSubviews(of: UIImageView.self).filter { imageView in
            imageView.bounds.width >= 12 &&
                imageView.bounds.height >= 12 &&
                imageView.bounds.width <= 44 &&
                imageView.bounds.height <= 44 &&
                !imageView.isHidden
        }
        .toolbar(
            usesNativeTabBar &&
                wrapper.route.keepsTabBar &&
                !appCoordinator.usesGlassTabBarOverlay &&
                appCoordinator.isNativeTabBarVisible
                ? Visibility.visible
                : Visibility.hidden,
            for: .tabBar
        )
        .toolbar(
            wrapper.route.hidesNavigationBar ? Visibility.hidden : Visibility.visible,
            for: .navigationBar
        )
        // No frosted strip across the top of any page. The bar keeps its buttons
        // (back, title) but loses its blurred background and the blur iOS adds along the
        // top edge, so the page shows right up to the top of the screen.
        .toolbarBackground(.hidden, for: .navigationBar)
        .hidesTopEdgeBlur()
        .verticalBarDisabled(wrapper.route is PlayerRoute)
    }

        guard let imageView = imageViews.min(by: {
            nativeProfileImageScore($0, in: profileTabButton) < nativeProfileImageScore($1, in: profileTabButton)
        }) else {
            profileTabStaticImageView?.alpha = 1
            profileTabStaticImageView = nil
            return fallbackFrame
        }

private extension View {
    // Only the floating tab pill: no full-width blurred band behind it.
    @ViewBuilder
    func hidesTabBarBackground() -> some View {
        if #available(iOS 26.0, *) {
            toolbarBackground(.hidden, for: .tabBar)
                .scrollEdgeEffectHidden(true, for: .bottom)
        } else {
            toolbarBackground(.hidden, for: .tabBar)
        }
    }

    @ViewBuilder
    func hidesTopEdgeBlur() -> some View {
        if #available(iOS 26.0, *) {
            scrollEdgeEffectHidden(true, for: .top)
        } else {
            self
        }
    }

    @ViewBuilder
    func verticalBarDisabled(_ disabled: Bool) -> some View {
        if #available(iOS 27.1, *), disabled {
            toolbarVerticalBehavior(.disabled)
        } else {
            self
        }
    }
}

@available(iOS 26.0, *)
private struct NativeProfileItem: Identifiable, Equatable {
    let id: Int32
    let name: String
    let avatarColor: UIColor
    let avatarBackgroundColor: UIColor
    let avatarURL: URL?
    let pinEnabled: Bool
    let active: Bool

    init(_ option: NativeProfileOption) {
        id = option.profileIndex
        name = option.name
        avatarColor = UIColor(hexString: option.avatarColorHex)
            ?? UIColor(red: 30 / 255, green: 136 / 255, blue: 229 / 255, alpha: 1)
        avatarBackgroundColor = UIColor(hexString: option.avatarBackgroundColorHex)
            ?? avatarColor.withAlphaComponent(0.16)
        avatarURL = option.avatarImageUrl.flatMap(URL.init(string:))
        pinEnabled = option.pinEnabled
        active = option.active
    }

    private func profileTabFallbackAvatarFrame(in tabButton: UIView) -> CGRect {
        let buttonFrame = tabButton.convert(tabButton.bounds, to: view)
        let side: CGFloat = 28
        let center = CGPoint(
            x: buttonFrame.midX,
            y: currentNativeSelectedTab == .settings ? buttonFrame.midY - 14 : buttonFrame.minY + 24
        )
        return CGRect(
            x: center.x - side / 2,
            y: center.y - side / 2,
            width: side,
            height: side
        )
    }

    private func nativeProfileImageScore(_ imageView: UIImageView, in tabButton: UIView) -> CGFloat {
        let frame = imageView.convert(imageView.bounds, to: tabButton)
        let horizontalDistance = abs(frame.midX - tabButton.bounds.midX)
        let sizeDistance = abs(max(frame.width, frame.height) - 28)
        let lowerHalfPenalty: CGFloat = frame.midY > tabButton.bounds.midY ? 20 : 0
        return horizontalDistance + sizeDistance + lowerHalfPenalty
    }

    private func nativeTabButton(for tab: NativeTab) -> UIControl? {
        let tabs = visibleNativeTabs
        guard
            let tabIndex = tabs.firstIndex(of: tab),
            tabBar.bounds.width > 0
        else {
            return nil
        }

    func choose(_ profile: NativeProfileItem, onComplete: @escaping () -> Void) {
        if profile.pinEnabled && !profile.active {
            lockedProfile = profile
            pin = ""
            errorMessage = nil
        } else {
            visualIndex = CGFloat(tabIndex)
        }
        let expectedCenterX = itemWidth * (visualIndex + 0.5)

        return tabBar.subviews.compactMap { $0 as? UIControl }
            .filter { !$0.isHidden && $0.bounds.width > 0 && $0.bounds.height > 0 }
            .min {
                abs($0.convert($0.bounds, to: tabBar).midX - expectedCenterX) <
                    abs($1.convert($1.bounds, to: tabBar).midX - expectedCenterX)
            }
    }

    private func refreshProfileAvatarImageIfNeeded() {
        let urlString = UserDefaults.standard.string(forKey: Self.nativeProfileAvatarURLKey)
        guard urlString != profileAvatarImageURL else { return }

        profileAvatarImageTask?.cancel()
        profileAvatarImageTask = nil
        profileAvatarImageURL = urlString
        profileAvatarImage = nil
        profileAvatarAnimationSourceFrames = []
        profileAvatarAnimationSourceDuration = 0
        clearProfileTabAvatarAnimation()

        guard let urlString, let url = URL(string: urlString) else { return }

        profileAvatarImageTask = URLSession.shared.dataTask(with: url) { [weak self] data, _, _ in
            guard
                let self,
                let data,
                let payload = Self.profileAvatarImage(from: data)
            else { return }

            DispatchQueue.main.async {
                guard self.profileAvatarImageURL == urlString else { return }
                self.profileAvatarImage = payload.image
                self.profileAvatarAnimationSourceFrames = payload.animationFrames
                self.profileAvatarAnimationSourceDuration = payload.animationDuration
                self.applyNativeTabBarAppearance()
            }
        }
        profileAvatarImageTask?.resume()
    }

    private static func profileAvatarImage(from data: Data) -> ProfileAvatarImagePayload? {
        guard let source = CGImageSourceCreateWithData(data as CFData, nil) else {
            return UIImage(data: data).map {
                ProfileAvatarImagePayload(image: $0, animationFrames: [], animationDuration: 0)
            }
        }

        let frameCount = CGImageSourceGetCount(source)
        guard frameCount > 0 else {
            return UIImage(data: data).map {
                ProfileAvatarImagePayload(image: $0, animationFrames: [], animationDuration: 0)
            }
        }

    private var usesNativeTabBar: Bool {
        let idiom = UIDevice.current.userInterfaceIdiom
        guard idiom == .phone || idiom == .pad else {
            return false
        }
        if #available(iOS 26.0, *) {
            return appCoordinator.tabBarBehavior.isEnabled
        }
        return false
    }

        if frameCount > 1 {
            var frames: [UIImage] = []
            var duration: TimeInterval = 0

            for index in 0..<frameCount {
                guard let cgImage = CGImageSourceCreateThumbnailAtIndex(source, index, thumbnailOptions as CFDictionary) ??
                    CGImageSourceCreateImageAtIndex(source, index, nil)
                else {
                    continue
                }
                frames.append(UIImage(cgImage: cgImage, scale: UIScreen.main.scale, orientation: .up))
                duration += gifFrameDelay(source: source, index: index)
            }

    @State private var mountedLegacyTabs: Set<NuvioAppTab> = []

    private var legacyTabs: some View {
        ZStack {
            ForEach(appCoordinator.availableTabs, id: \.self) { tab in
                if mountedLegacyTabs.contains(tab) {
                    TabContentView(
                        tab: tab,
                        usesNativeTabBar: usesNativeTabBar,
                        usesTabletFloatingTabBar: usesTabletFloatingTabBar,
                        coordinator: appCoordinator.coordinator(for: tab),
                        appCoordinator: appCoordinator
                    )
                    .opacity(appCoordinator.selectedTab == tab ? 1 : 0)
                    .allowsHitTesting(appCoordinator.selectedTab == tab)
                    .accessibilityHidden(appCoordinator.selectedTab != tab)
                    .zIndex(appCoordinator.selectedTab == tab ? 1 : 0)
                }
            }
        }
        .onAppear {
            mountedLegacyTabs.insert(appCoordinator.selectedTab)
        }
        .onChange(of: appCoordinator.selectedTab) { newTab in
            mountedLegacyTabs.insert(newTab)
        }
    }

    @available(iOS 26.0, *)
    private var nativeTabs: some View {
        TabView(selection: tabSelection) {
            ForEach(appCoordinator.availableTabs, id: \.self) { tab in
                if tab == .settings {
                    Tab(value: tab) {
                        TabContentView(
                            tab: tab,
                            usesNativeTabBar: usesNativeTabBar,
                            usesTabletFloatingTabBar: usesTabletFloatingTabBar,
                            coordinator: appCoordinator.coordinator(for: tab),
                            appCoordinator: appCoordinator
                        )
                        .hidesTabBarBackground()
                    } label: {
                        Label {
                            Text(appCoordinator.title(for: tab))
                        } icon: {
                            Image(
                                uiImage: iconStore.image(
                                    for: tab,
                                    selected: appCoordinator.selectedTab == tab
                                )
                            )
                            .id(
                                "\(tab.rawValue)-\(iconStore.revision)-" +
                                    "\(appCoordinator.selectedTab == tab)"
                            )
                        }
                    }
                    .popover(
                        isPresented: $appCoordinator.isProfileSwitcherPresented,
                        attachmentAnchor: .rect(.bounds),
                        arrowEdge: .bottom
                    ) {
                        NativeProfileSwitcherView(
                            controller: appCoordinator.profileSwitcherController,
                            title: appCoordinator.localizedSwitchProfileTitle,
                            addProfileTitle: appCoordinator.localizedAddProfileTitle,
                            onManageProfiles: appCoordinator.openProfileManagement
                        )
                    }
                } else {
                    Tab(value: tab) {
                        TabContentView(
                            tab: tab,
                            usesNativeTabBar: usesNativeTabBar,
                            usesTabletFloatingTabBar: usesTabletFloatingTabBar,
                            coordinator: appCoordinator.coordinator(for: tab),
                            appCoordinator: appCoordinator
                        )
                        .hidesTabBarBackground()
                    } label: {
                        Label {
                            Text(appCoordinator.title(for: tab))
                        } icon: {
                            Image(
                                uiImage: iconStore.image(
                                    for: tab,
                                    selected: appCoordinator.selectedTab == tab
                                )
                            )
                            .id(
                                "\(tab.rawValue)-\(iconStore.revision)-" +
                                    "\(appCoordinator.selectedTab == tab)"
                            )
                        }
                    }
                }
            }
        }
        .id(appCoordinator.isLiveTvTabVisible)
        .tint(Color(uiColor: iconStore.accentColor))
        .tabBarMinimizeBehavior(
            appCoordinator.tabBarBehavior == .autoHide ? .onScrollDown : .never
        )
        .overlay(alignment: .bottom) {
            if appCoordinator.usesGlassTabBarOverlay &&
                appCoordinator.isAppReady &&
                appCoordinator.isSelectedTabAtRoot {
                GeometryReader { proxy in
                    NuvioGlassTabBar(
                        appCoordinator: appCoordinator,
                        iconStore: iconStore
                    )
                    .frame(width: proxy.size.width)
                    .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .bottom)
                }
                .ignoresSafeArea(.all)
            }
        }
        .ignoresSafeArea(.container, edges: .bottom)
    }

    @ViewBuilder
    var body: some View {
        ZStack {
            Group {
                if appCoordinator.isMainContentMounted {
                    if #available(iOS 26.0, *), usesNativeTabBar {
                        nativeTabs
                    } else {
                        legacyTabs
                    }
                } else {
                    Color(uiColor: nuvioBackgroundColor)
                        .ignoresSafeArea(.all)
                }
            }
            .zIndex(0)

            AppGateComposeView(appCoordinator: appCoordinator)
                .ignoresSafeArea(.all)
                .allowsHitTesting(!appCoordinator.isAppReady)
                .accessibilityHidden(appCoordinator.isAppReady)
                .zIndex(1)
        }
        .onReceive(
            NotificationCenter.default.publisher(
                for: Notification.Name("NuvioNativeTabChromeDidChange")
            )
        ) { _ in
            appCoordinator.reloadTabBarBehavior()
            appCoordinator.reloadTabBarVisibility()
        }
        .onReceive(
            NotificationCenter.default.publisher(
                for: Notification.Name("NuvioLiveTvNavigationVisibilityDidChange")
            )
        ) { _ in
            appCoordinator.reloadLiveTvTabVisibility()
        }

struct ContentView: View {
    @Environment(\.scenePhase) private var scenePhase
    @ObservedObject private var systemUI = SystemUI.shared

    var body: some View {
        Group {
            if #available(iOS 16.0, *) {
                NativeNavContentView()
                    .accessibilityHidden(systemUI.pictureInPictureMessage != nil)
                    .allowsHitTesting(systemUI.pictureInPictureMessage == nil)
            } else {
                ComposeView()
                    .ignoresSafeArea(.all)
                    .accessibilityHidden(systemUI.pictureInPictureMessage != nil)
                    .allowsHitTesting(systemUI.pictureInPictureMessage == nil)
            }
        }
        .persistentSystemOverlays(systemUI.isPlayerImmersive ? .hidden : .automatic)
        .statusBarHidden(systemUI.isPlayerImmersive)
        .overlay {
            if let message = systemUI.pictureInPictureMessage {
                Color.black
                    .ignoresSafeArea()
                    .overlay {
                        VStack(spacing: 16) {
                            Image(uiImage: AVPictureInPictureController.pictureInPictureButtonStartImage)
                                .renderingMode(.template)
                                .resizable()
                                .scaledToFit()
                                .frame(width: 96, height: 72)
                                .accessibilityHidden(true)
                            Text(message)
                                .font(.body)
                                .multilineTextAlignment(.center)
                                .fixedSize(horizontal: false, vertical: true)
                        }
                        .foregroundStyle(.gray)
                        .padding(24)
                        .accessibilityElement(children: .combine)
                    }
            }
        }
        .onChange(of: scenePhase) { phase in
            if phase == .active {
                DispatchQueue.main.async {
                    NotificationCenter.default.post(name: playerSceneDidActivateNotification, object: nil)
                }
            }
        }
    }
}

private extension UIColor {
    convenience init?(hexString: String?) {
        guard var value = hexString?.trimmingCharacters(in: .whitespacesAndNewlines), !value.isEmpty else {
            return nil
        }
        if value.hasPrefix("#") {
            value.removeFirst()
        }
        guard value.count == 6, let rgb = UInt64(value, radix: 16) else {
            return nil
        }
        self.init(
            red: CGFloat((rgb >> 16) & 0xFF) / 255,
            green: CGFloat((rgb >> 8) & 0xFF) / 255,
            blue: CGFloat(rgb & 0xFF) / 255,
            alpha: 1
        )
    }
}

private extension String {
    var nonEmpty: String? {
        isEmpty ? nil : self
    }
}

private extension UIView {
    func recursiveSubviews<T: UIView>(of type: T.Type) -> [T] {
        subviews.flatMap { subview -> [T] in
            var matches = (subview as? T).map { [$0] } ?? []
            matches.append(contentsOf: subview.recursiveSubviews(of: type))
            return matches
        }
    }
}

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        // Register MPV player bridge before Compose initializes
        NuvioPlayerRegistration.register()
        
        let controller = MainViewControllerKt.MainViewController()
        controller.view.backgroundColor = UIColor(red: 0.008, green: 0.016, blue: 0.016, alpha: 1.0)
        return RootComposeViewController(contentController: controller)
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        ComposeView()
            .ignoresSafeArea()
    }
}
