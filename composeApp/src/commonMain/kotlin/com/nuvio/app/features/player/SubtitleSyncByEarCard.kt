package com.nuvio.app.features.player
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Hearing
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nuvio.app.core.ui.nuvio
import nuvio.composeapp.generated.resources.Res
import nuvio.composeapp.generated.resources.compose_player_sync_by_ear
import org.jetbrains.compose.resources.stringResource
@Composable
internal fun SubtitleSyncByEarCard(visible:Boolean,subtitleDelayMs:Int,heardCaptured:Boolean,sawCaptured:Boolean,onHeard:()->Unit,onSaw:()->Unit,onClose:()->Unit,modifier:Modifier=Modifier){
 val tokens=MaterialTheme.nuvio
 AnimatedVisibility(visible=visible,modifier=modifier,enter=fadeIn()+slideInVertically{-it/4},exit=fadeOut()+slideOutVertically{-it/4}){
  Column(modifier=Modifier.widthIn(max=340.dp).fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Color(0xFF17171A).copy(alpha=.94f)).border(1.dp,Color.White.copy(alpha=.1f),RoundedCornerShape(20.dp)).padding(14.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){
   Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.SpaceBetween){
    Text(stringResource(Res.string.compose_player_sync_by_ear),color=Color.White,style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.SemiBold)
    Box(Modifier.size(32.dp).clip(CircleShape).background(Color.White.copy(alpha=.08f)).clickable(onClick=onClose),contentAlignment=Alignment.Center){Icon(Icons.Rounded.Close,null,tint=Color.White)}
   }
   Text("Tap Heard when you hear the line, then Saw when its subtitle appears.",color=Color.White.copy(alpha=.72f),style=MaterialTheme.typography.bodySmall)
   Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(10.dp)){
    Row(Modifier.weight(1f).clip(RoundedCornerShape(14.dp)).background(if(heardCaptured)tokens.colors.accent else Color.White.copy(alpha=.1f)).clickable(onClick=onHeard).padding(12.dp),horizontalArrangement=Arrangement.spacedBy(8.dp),verticalAlignment=Alignment.CenterVertically){Icon(Icons.Rounded.Hearing,null,tint=Color.White);Text("Heard",color=Color.White)}
    Row(Modifier.weight(1f).clip(RoundedCornerShape(14.dp)).background(if(sawCaptured)tokens.colors.accent else Color.White.copy(alpha=.1f)).clickable(onClick=onSaw).padding(12.dp),horizontalArrangement=Arrangement.spacedBy(8.dp),verticalAlignment=Alignment.CenterVertically){Icon(Icons.Rounded.Visibility,null,tint=Color.White);Text("Saw",color=Color.White)}
   }
   Text(if(subtitleDelayMs==0)"Current delay: in sync" else "Current delay: "+subtitleDelayMs+" ms",color=Color.White.copy(alpha=.65f),style=MaterialTheme.typography.labelMedium)
  }
 }
}
internal fun PlayerScreenRuntime.applySubtitleSyncByEarIfReady(){val heardMs=subtitleSyncHeardPositionMs?:return;val sawMs=subtitleSyncSawPositionMs?:return;setSubtitleDelay((subtitleDelayMs.toLong()+heardMs-sawMs).coerceIn(SUBTITLE_DELAY_MIN_MS.toLong(),SUBTITLE_DELAY_MAX_MS.toLong()).toInt());subtitleSyncHeardPositionMs=null;subtitleSyncSawPositionMs=null}
