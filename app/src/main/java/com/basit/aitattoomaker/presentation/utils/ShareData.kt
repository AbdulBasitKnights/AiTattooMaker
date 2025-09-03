package com.basit.aitattoomaker.presentation.utils

import android.graphics.Bitmap
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.lifecycle.MutableLiveData
import com.basit.aitattoomaker.R
import com.basit.aitattoomaker.presentation.ai_create.model.StyleItem

var tattooCreation = MutableLiveData<Boolean?>()
var access_Token :String?=null
var capturedBitmap: Bitmap?=null
var editedBitmap: Bitmap?=null
@DrawableRes var selectedTattoo: Int?=null

val style_list: ArrayList<StyleItem> = arrayListOf(
    StyleItem("12", "No Style", false,true,"file:///android_asset/styles/nostyle.webp","Simple clean execution, straightforward design, universal appeal, balanced proportions, clear readability"),
    StyleItem("1", "Anime", false,false,"file:///android_asset/styles/anime.webp","Bold outlines, vibrant colors, exaggerated expressions, dynamic poses, stylized features, flat shading"),
    StyleItem("3", "Engraving", true,false,"file:///android_asset/styles/engraving.webp","Fine engraved lines, cross-hatching, intricate detailing, vintage etching style, monochrome palette, classic shading"),
    StyleItem("4", "Flames", false,false,"file:///android_asset/styles/flames.webp","bold clean outlines, dynamic flame shapes wrapping around the subject, shades of black  red, realistic flame textures blended with stylized tattoo edges, high contrast body-art aesthetic."),
    StyleItem("5", "Geometric", false,false,"file:///android_asset/styles/geometric.webp","Symmetrical patterns, sharp angles, precise shapes, mathematical balance, interlocking elements, clean design"),
    StyleItem("6", "Abstract", false,false,"file:///android_asset/styles/abstract.webp","Freeform designs without strict realism. Uses shapes, swirls, and unique compositions"),
    StyleItem("7", "Graffiti", false,false,"file:///android_asset/styles/graffiti.webp","Bold graffiti lettering, vibrant street colors, dynamic spray effects, layered textures, urban aesthetic"),
    StyleItem("8", "Blackout", false,false,"file:///android_asset/styles/blackout.webp","Uses solid black ink to create bold areas, sometimes leaving negative space to form designs"),
    StyleItem("9", "Celtic", false,false,"file:///android_asset/styles/celtic.webp","Knots, spirals, and interwoven designs inspired by Celtic art and symbolism."),
    StyleItem("10", "Minimalist", false,false,"file:///android_asset/styles/minimalist.webp","Clean lines, simple shapes, minimal detail, balanced composition, modern aesthetic"),
    StyleItem("11", "Motives", false,false,"file:///android_asset/styles/motives.webp","Symbolic elements, personal icons, intricate details, meaningful imagery, artistic composition, balanced layou"),
    StyleItem("13", "Dot Work", false,false,"file:///android_asset/styles/dotwork.webp","Uses small dots to create shading, gradients, and textures.Often geometric or spiritual designs."),
    StyleItem("14", "Realistic", false,false,"file:///android_asset/styles/realistic.webp","Photorealistic details, lifelike textures, realistic proportions, fine shading, depth, high clarity"),
    StyleItem("15", "Sketch", false,false,"file:///android_asset/styles/sketch.webp","Loose pencil strokes, visible outlines, dynamic sketch marks, unfinished look, raw artistic energy"),
    StyleItem("16", "Floral", false,false,"file:///android_asset/styles/floral.webp","Detailed, decorative designs of flowers, leaves, and vines.Can be realistic or stylized"),
    StyleItem("17", "Trash Polka", false,false,"file:///android_asset/styles/trashpolka.webp","photorealism, abstract elements, bold red and black, grunge textures, chaotic brush strokes"),
    StyleItem("18", "Gem Ink", false,false,"file:///android_asset/styles/gemink.webp","Shiny, jewel-like coloring using ruby red, sapphire blue, and emerald green tones with reflective highlights to give a crystal effect"),
    StyleItem("19", "Victorian", false,false,"file:///android_asset/styles/victorian.webp","Ornate details, elegant patterns, floral motifs, intricate lacework, classical composition, vintage aesthetic"),
    StyleItem("20", "Water Color", false,false,"file:///android_asset/styles/watercolor.webp","Soft, paint-like (red, blue, purple tattoo subject, mimicking watercolor art"),
    StyleItem("21", "Iron Line", false,false,"file:///android_asset/styles/ironline.webp","Strong, heavy black outlines like wrought iron shapes, filled with deep blue or red accents for contrast"),
    StyleItem("22", "Line Art", false,false,"file:///android_asset/styles/lineart.webp","Clean, single continuous lines with no shading or heavy details.Modern, elegant, and minimal."),
    StyleItem("23", "Line Vine", false,false,"file:///android_asset/styles/linevine.webp","Flowing vine-like outlines , filled with blue or red details"),
    StyleItem("24", "Mandala", false,false,"file:///android_asset/styles/mandala.webp","Symmetrical circular designs inspired by spiritual and cultural symbols. Highly detailed and ornamental"),
    StyleItem("25", "Neo Color", false,false,"file:///android_asset/styles/neocolor.webp","Strong, heavy black outlines like wrought iron shapes, filled with deep blue or red accents for contrast"),
    StyleItem("26", "Pop Art", false,false,"file:///android_asset/styles/popart.webp","Uses comic-book aesthetics with bold lines, halftone dots, and exaggerated pop culture vibes"),
)

val styleLiveData: MutableLiveData<ArrayList<StyleItem>> = MutableLiveData(style_list)