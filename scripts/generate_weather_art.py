from pathlib import Path
from html import escape
out=Path('app/src/main/res/drawable'); src=Path('design/weather')
# Original filled vector artwork. Same paths/gradients exported to Android and SVG.
gradients={'cloud':('#FFFFFF','#9DB8D2'),'back':('#D7E7F5','#7896B6'),'sun':('#FFF3B5','#F4B24C'),'moon':('#F4F9FF','#AFC8E8'),'rain':('#A5E7FF','#4B9DDA'),'snow':('#FFFFFF','#B3DCED'),'fog':('#DCEBF6','#8FADCA'),'shadow':('#526C89','#526C89')}
def ellipse(cx,cy,rx,ry): return f'M{cx-rx},{cy}a{rx},{ry} 0,1 0,{2*rx},0a{rx},{ry} 0,1 0,{-2*rx},0'
def cloud(back=False):
    if back:return [('back','M27,62C16,62 13,47 22,40C22,24 43,19 53,31C66,23 81,34 80,47C92,52 87,66 76,66Z',1)]
    return [('shadow',ellipse(65,86,44,6),.13),('cloud','M31,80C12,80 11,54 28,50C27,31 53,24 65,41C80,29 99,41 98,56C116,56 119,80 98,83L34,83Z',1),('moon','M31,54C30,38 51,31 62,44C50,39 36,45 34,56Z',.65)]
def sun(small=False):
    x,y,r=(83,30,19) if small else (64,52,29)
    return [('sun',ellipse(x,y,r+7,r+7),.13),('sun',ellipse(x,y,r,r),1),('moon',ellipse(x-7,y-9,9,6),.24)]
def moon(small=False):
    return [('moon','M91,9C65,4 53,37 72,53C84,64 104,59 111,45C89,52 74,29 91,9Z' if small else 'M79,16C46,7 25,45 43,72C59,94 91,87 104,65C75,77 51,45 79,16Z',1)]
def drops(drizzle=False):
    return [('rain',ellipse(x,96,2.4,3.5) if drizzle else f'M{x},87C{x-2},92 {x-7},97 {x-3},101C{x+3},105 {x+6},98 {x},87Z',1) for x in (42,63,84)]
def snow():
    return [('snow',f'M{x-1},88h2v5l4,-3l1,2l-4,3l4,3l-1,2l-4,-3v5h-2v-5l-4,3l-1,-2l4,-3l-4,-3l1,-2l4,3Z',1) for x in (43,65,87)]
art={'clear_day':sun(),'clear_night':moon(),'partly_day':sun(True)+cloud(),'partly_night':moon(True)+cloud(),'cloudy':cloud(True)+cloud(),'rain':cloud()+drops(),'drizzle':cloud()+drops(True),'snow':cloud()+snow(),'sleet':cloud()+drops()[:1]+snow()[1:],'thunder':cloud()+[('sun','M67,75L53,95H64L57,113L83,88H70L80,75Z',1)],'fog':cloud()+[('fog',f'M{x},{y}h{w}a2,2 0,0 1,0 4h-{w}a2,2 0,0 1,0 -4',.85) for x,y,w in [(22,89,78),(34,99,64),(19,109,64)]],'unknown':[('back',ellipse(64,60,30,30),1),('moon','M55,49C56,37 76,37 77,49C78,58 65,59 65,67H60C58,56 71,55 71,49C71,43 61,43 61,49Z',1),('moon',ellipse(63,77,3,3),1)]}
for name,paths in art.items():
    xml=['<vector xmlns:android="http://schemas.android.com/apk/res/android" xmlns:aapt="http://schemas.android.com/aapt" android:width="128dp" android:height="128dp" android:viewportWidth="128" android:viewportHeight="128">']
    svg=['<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 128 128"><defs>']
    for k,(a,b) in gradients.items():svg.append(f'<linearGradient id="{k}" gradientUnits="userSpaceOnUse" x1="36" y1="24" x2="82" y2="100"><stop stop-color="{a}"/><stop offset="1" stop-color="{b}"/></linearGradient>')
    svg.append('</defs>')
    for k,d,alpha in paths:
        a,b=gradients[k];xml.append(f'<path android:pathData="{d}" android:fillAlpha="{alpha}"><aapt:attr name="android:fillColor"><gradient android:startX="36" android:startY="24" android:endX="82" android:endY="100" android:startColor="{a}" android:endColor="{b}" android:type="linear"/></aapt:attr></path>')
        svg.append(f'<path d="{d}" fill="url(#{k})" opacity="{alpha}"/>')
    xml.append('</vector>');svg.append('</svg>');(out/f'art_weather_{name}.xml').write_text('\n'.join(xml)+'\n');(src/f'{name}.svg').write_text(''.join(svg))
