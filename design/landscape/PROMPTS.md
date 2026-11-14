# Maisemataustat, built-in image_gen, 14.11.2026

Toteutus käyttää sisäänrakennettua image_gen-työkalua, ei API/CLI-fallbackia. Kuvat ovat koristeellisia eivätkä kuvaa käyttäjän todellista sijaintia. Generoidut 1024 × 1536 kuvat pienennettiin 768 × 1152 WebP-resursseiksi (quality 88), yhteensä noin 41 kt. Mittasuhde säilytettiin. Sovelluksen tekstit, säätiedot, kuvakkeet ja painikkeet piirretään erikseen Composella.

Tallennetut, sovelluksen käyttämät tiedostot:
- `app/src/main/res/drawable-nodpi/weather_landscape_dark.webp`
- `app/src/main/res/drawable-nodpi/weather_landscape_light.webp`

## Tumma — lopullinen generointikehote

Use case: stylized-concept. Project asset, no UI mockup: one premium textless portrait 1024x1536 background illustration for an Android weather panel DARK THEME. Atmospheric Nordic blue-hour mountain landscape. Top third very dark midnight navy sky (#101f33), calm empty space for real UI text. Soft diffuse blue ambient illumination toward middle-left (no bright glowing disk). Lower middle contains FIVE natural irregular rounded mountain/fell ridge silhouettes at different distances, distant ridges pale desaturated blue-grey and misty, near ridges darker navy with softly detailed edges, atmospheric thin mist between ridges. Sophisticated tranquil depth and smooth painterly gradients, not sine waves, not flat stripes. Bottom fifth smoothly becomes uniform deep slate navy (#22374d) so UI can fade into it. No snowstorm, no stars, moon, sun, lightning, clouds as weather icons, buildings, trees, humans, text, logos or borders. Weather icons and all interface elements are added separately by code. Landscape decorative, no exact real location. Save generated file for project use.

## Vaalea — lopullinen muokkauskehote

Viite ja muokkauskohde: yllä generoitu tumma kuva.

Create LIGHT THEME version of attached decorative Android weather background. Preserve exactly the same portrait composition and irregular mountain/fell silhouettes, atmospheric mist, natural textures and smooth depth. Change palette to bright airy pale grey-blue sky, diffuse soft light, distant very light misty blue-grey ridges, nearer slightly deeper muted blue-grey ridges, NO green. Keep upper third light and calm for dark real UI text. Bottom fifth gradually becomes uniform very pale grey-blue #EAF2FA. No text, logos, UI, stars, sun, moon, people, buildings or new elements. High quality soft atmospheric landscape illustration, subtle low contrast. This is a standalone offline app asset, not a mockup.

## Tarkistuskuva

`resource-preview.png` on paikallisten resurssien ja niitä vastaavien kokojen HTML/Chrome-sommittelutarkistus, ei Compose- tai Android-kuvakaappaus. Tarkistettu 360 px vaalea/tumma ja 280 px suurempi lähdeteksti, 26/30 px kuvakkeet, suhteet ja alkuperäisvärit. Kuvan esimerkkisäätä ei käytetä sovelluksessa. Todellisen Android-layoutin rivittyminen, insetit, vieritys ja fonttiskaalaus jäävät laitteella tarkistettaviksi.
