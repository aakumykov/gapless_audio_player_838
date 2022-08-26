### Библиотека для бесшовного воспроизведения списка звуковых файлов на Android.

**Смысл**

Создана на основе Android MediaPlayer, хитрыми манипуляциями с его возможностью предзагрузки следужщего трека.
Чистый MediaPlayer воспроизводит список с паузой между треками. 
Класс SoundPool мог бы играть без задержек, но ограничен размером файла в 1Мб.

**Подключение**

Добавьте в файл build.gragle модуля:

`implementation 'com.github.aakumykov:gapless_audio_player_838:Tag'`

**Использование**

Пример проекта в ветке demo.
Mp3-файлы кладите в каталог Downloads.

**Страница на Jitpack**

https://jitpack.io/#aakumykov/gapless_audio_player_838

[![](https://jitpack.io/v/aakumykov/gapless_audio_player_838.svg)](https://jitpack.io/#aakumykov/gapless_audio_player_838)
