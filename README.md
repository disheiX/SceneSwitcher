# A simple Scene Switcher Plugin for Jingle
A simple plugin made for the [Jingle](https://github.com/DuncanRuns/Jingle) app. It allows you to switch scenes on OBS with a hotkey. It might be useful for customizing/animating scenes when measuring or using thin/wide.


## Setup
* Download the plugin by going to the [release section](https://github.com/disheiX/SceneSwitcher/releases/latest)
* Put it inside the Jingle Plugins Folder
* Press the button `Copy script path to clipboard` at the plugin tab
* Add the script to OBS by going to `Tools > Scripts`


## Usage
### Default Scenes
Set the name of your OBS scene for the playing scene (Default is `Playing`). This scene will be toggleable with the other scenes. Then, do the same for the rest, it is not needed to fill in all fields in case you won't use them all. 

### More Scenes
Let's say, you won't use this plugin for resizing purposes. In such case, press the `Add State` button and put a name in the dialog. Notice this is NOT your scene name, but the OBS state label.

## Features
* Toggle to any OBS Scene when the window gets resized.
* Add and remove OBS Scenes for custom purposes (not specifically resizing).
* Uses your already existing hotkeys from the built-in Jingle system.
* Saves your settings in a file.

## Disclaimer
I did this plugin for fun and to learn a little bit more of Java. I haven't done extensive tests on this, there might be some issues so I don't guarantee anything. Feel free to fork or do PR's
