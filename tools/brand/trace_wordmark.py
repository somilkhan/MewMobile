"""Trace the "Mew" lettering out of docs/mew-banner.png into tools/brand/source/mew_wordmark.path.
Run from the repo root. Needs pillow, numpy, scipy, potracer."""
from PIL import Image, ImageFilter
import numpy as np, potrace, scipy.ndimage as nd, cairosvg
im=Image.open('docs/mew-banner.png').convert('L')
c=im.crop((680,90,1290,252))
S=6
big=c.resize((c.width*S,c.height*S),Image.BICUBIC).filter(ImageFilter.GaussianBlur(4))
b=np.asarray(big)>128
lab,n=nd.label(b); sizes=nd.sum(b,lab,range(1,n+1))
keep=np.isin(lab,[i+1 for i,s in enumerate(sizes) if s>20000])
ys,xs=np.where(keep); print('bbox',xs.min(),xs.max(),ys.min(),ys.max(), keep.shape)
keep=keep[ys.min():ys.max()+1, xs.min():xs.max()+1]
keep=np.pad(keep,20)
bm=potrace.Bitmap(~keep)
path=bm.trace(turdsize=50, alphamax=1.0, opticurve=True, opttolerance=0.3)
H,W=keep.shape
f=lambda p:f"{p.x-20:.1f} {p.y-20:.1f}"
d=[]
for curve in path:
    d.append("M"+f(curve.start_point))
    for seg in curve.segments:
        d.append(("L"+f(seg.c)+" L"+f(seg.end_point)) if seg.is_corner else ("C"+f(seg.c1)+" "+f(seg.c2)+" "+f(seg.end_point)))
    d.append("Z")
D=" ".join(d); W-=40; H-=40
open('tools/brand/source/mew_wordmark.path','w').write(f"{W} {H}\n{D}")
svg=f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {W} {H}"><path fill-rule="evenodd" fill="#F5EBDD" d="{D}"/></svg>'
print(W,H,len(path))

