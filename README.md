# QuoteTube Beta 1.3
## Description
QuoteTube is a simple soundbox tool in which you can extract a part of a youtube video and play "quotes" (the extracted sound) indefinitely without Internet. QuoteTube doesn't use a server for processing the different steps but will use instead Youtube-dl (through QPython app) and FFmpeg libs.

[Download it here](https://klemek.fr/quotetube/quotetube-beta1.3.apk)
## Current features

* Offline use of created quotes
* Whole youtube search
* Customizable quote with color and name
* Full editor interface
  * Relative navigation through video
  * Precises start and stop points placing
  * Try quote before creating
  * Customize quote appearance and name
* 1-20 secs quotes
* 1 sec fade-out option
* Quote deletion

## Future features

* Quote organization with drag-n-drop
* Quote marketplace for sharing and downloading other's quotes
* Quote image taken from thumbnail or user's files (or video itself)

## Changelog
### Beta 1.3

* Fixed last QPython sh!t by downloading and installing a working version (1.2.5) on the user's phone
* Fixed quote edition when buttons weren't working sometimes
* Splitted quote creation into 2 phases (positions and informations)

## Libs

* [FFmpeg Android](http://writingminds.github.io/ffmpeg-android-java/)
* [youtube-dl](https://rg3.github.io/youtube-dl/)
* [GeometricProgressView](https://android-arsenal.com/details/1/5376)
* [ColorPicker](https://android-arsenal.com/details/1/5067)
* [MaterialDialog](https://github.com/afollestad/material-dialogs)
* [Picasso](http://square.github.io/picasso/)
