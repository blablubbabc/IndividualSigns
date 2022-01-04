# Changelog
Date format: (YYYY-MM-DD)  

## Next release
### Supported MC versions: xxx

## v2.9.1 Release (TBA)
### Supported MC versions: 1.18.1


## v2.9.0 Release (2022-01-04)
### Supported MC versions: 1.18.1
* Bumped the Bukkit dependency to Spigot 1.18.1. This version is not compatible with older Bukkit versions.
* Bumped the ProtocolLib dependency to version 4.8.0.
* Similar to how we update the nearby signs when a player joins the server, we now also update the nearby signs for players that are already online when the plugin is enabled.
* We now load all plugin classes right away. This avoids class loading issues when the plugin jar is dynamically replaced while the plugin is still running.
* Internal: Updated to Gradle 7.3.3.

## v2.8.0 Release (2021-08-08)
### Supported MC versions: 1.17.1
* Bumped the Bukkit dependency to 1.17.1. This version is not compatible with older Bukkit versions.
* Bumped the ProtocolLib dependency to version 4.7.0.
* Fixed: Sign text sent after the join delay might be outdated.
* Fixed: Sign updates did not take text color of dyed signs into account.
* Fixed: Sign updates did not take the glowing state (MC 1.17) into account.
* API: Deprecated SimpleChange#getValue(Player, Location, String) and added a replacement #getValue(Player, Location). This method is only invoked once per sent sign now and no longer provides the original sign line. The returned value is reused for all occurrences of the key within the text of the sent sign.
* Internal: Updated bStats to version 2.2.1.
* Internal: Updated to Gradle 7.1.1.
* Internal: Various minor refactors and cleanup.

## v2.7.0 Release (2019-06-17)
### Supported MC versions: 1.14
* Updated for MC 1.14. Support for MC 1.13.x was dropped.
* Updated bstats to version 1.5.

## v2.6.0 Release (2018-09-27)
### Supported MC versions: 1.13.1
* Updated for MC 1.13.1. This update does not work below MC 1.13.1!
* Built for ProtocolLib v4.4.0.
* Removed a bunch of old and outdated API.
* Switched from mcstats to bstats metrics (see https://bstats.org/plugin/bukkit/InSigns).
* Internal: Changed the group id from 'de.blablubbabc' to 'de.blablubbabc.insigns'.

## v2.5 Release (2016-12-22)
### Supported MC versions: 1.12, 1.11, 1.10, 1.9
* Using java 8 now. Make sure you have java 8 (or above) installed on your server.
* Updated to MC 1.11: The tile entity id for signs was changed. Signs should now get updated for chunk packets again.
* Added: When a player joins, nearby player signs now get updated additionally after a short delay (2 ticks), to give plugins (like Essentials) the chance to update the player's display name (or other data). The delay in ticks can be configured in the config via 'player-join-sign-update-delay' (default: 2), or disabled by setting it to 0.

## v2.4 Release (2016-05-15)
### Supported MC versions: 1.9
* Updated to MC 1.9.4: The sign contents need to additionally get replaced inside the chunk data packets now.

This requires the latest version of ProtocolLib (4.0.0-SNAPSHOT currently). If you run into any issues, let me know.

Does not work on versions below 1.9.4!

## v2.3 Release (2015-01-26)
### Supported MC versions: 1.8.1, 1.8
* Fixed: NPE when sending signs with empty lines which were created prior to MC 1.8 

## v2.2 Release (2015-01-24)
### Supported MC versions: 1.8.1, 1.8
* Removed debug print which was missed.

## v2.1 Release (2015-01-02)
### Supported MC versions: 1.8.1, 1.8
* The SignSendEvent now ignores line changes if the line wasn't actually changed.
* When a player places a sign we now stop scanning the lines of that sign after we find the first tag which the player has no permission for.
* InSigns.sendSignChange(Player, Sign) now uses bukkit's api for sending the sign update packet.
* Added new default replacement: [DISPLAY] gets replaced with a players display/nick name (permission: insigns.create.display)
* Update for MC (spigot) 1.8 and the 1.8 version of ProtocolLib (found here):
  * Removed 15 character limit for Changer and SimpleChanger keys: in MC 1.8 the sign line length now depends on the character widths of the client. Also sign lines can be even longer when using json-formatted lines.
  * Also removed the line wrapping feature because of that.
  * The lines of the SignSendEvent are now in json format as well. Also when you set lines those have to be in proper json-format as well. 

Does not work on versions below 1.8.x!

## v2.0 Release (2014-01-14)
### Supported MC versions: 1.7.4, 1.7.2
* **API rewrite:**
  * The old API was deprecated and internally replaced with equivalents using the new event based API in order to hopefully stay compatible to plugins still using the old API. The next update will remove those deprecated methods.
  * IndividualSigns does now simply call a SignSendEvent for each player being about to receive a sign update packet. This event can be cancelled and gives your more freedom on what can be done compared to before (your are not limited anymore on simple key->value replacements, but you can for example search for dynamic keys or replace the text of the whole sign, etc).
  * However, you will now have to process the lines yourself (checking for the key and replacing found matches), and you have to check the permission for the sign creation yourself as well. OR you use the included SimpleChanger class, which basically provides the same features like the old, now deprecated, Changer class: you simple create a new SimpleChanger and fill in the getValue() method (which now has a new 'line' parameter), and the class will do permission checks and simple key-value replacement for you, just like the old API, but more nicely. Check out the front page for examples on the new API.
* Added plugin usage tracking to mcstats.org 

Does not work on versions below 1.7.x!

## v1.5 Release (2013-12-18)
### Supported MC versions: 1.7.4, 1.7.2
* Updated to MC 1.7: Make sure to use the latest server and ProtocolLib versions.

Does not work on versions below 1.7.x!

## v1.4 Release (2013-05-04)
### Supported MC versions: 1.6.4, 1.6.2, 1.5.2
* Fixed: "unable to locate sign" -message when breaking a sign -> results in signs no longer being updated on rightclicking and if the interact event is cancelled. Thanks to lishid to explain me what causes this message!
* Fixed: Using multiple InSigns keys on one sign only display the value of the first changer in the list of changers.

## v1.3 Release (2013-04-20)
### Supported MC versions: 1.5.1, 1.5.0, 1.4.7
* getValue(..) of a Changer object will now get the location of the sign. Plugins using the API will break and will need to update.

Thanks to Flemingjp98 for this suggestion! 

## v1.2 Release (2013-03-30)
### Supported MC versions: 1.5.1, 1.5.0, 1.4.7
* getValue(..) of a Changer object will now get the complete player object as argument instead of only the name. Plugins using the API will break and will need to update.

Thanks to ragan262 for pointing this out!

## v1.1 Release (2013-03-05)
### Supported MC versions: 1.5.0, 1.4.7
* Changer only accepts keys smaller than 16 now.
* Performance improvement: Changers getValue() only gets called if needed (if the sign actually contains the key)
* Lines longer than 15 characters are now continued on the next lines if these are empty. 

Thanks to pratham2003 for his contribution!

## v1.0 Release (2013-02-15)
### Supported MC versions: 1.4.7
* Fixed a bug where players receive the package of another player. That was already fixed in an older version, but I removed the fixed in the newer one.. :/ sorry for that.. Now everything should work again like a charm :)

## v0.9 Release (2013-02-15)
### Supported MC versions: 1.4.7
* Changed and optimized most of the packet changing code and did some code/comment cleanup. This uses now comphenix packetwrapper and it now modifies the outgoing packet instead of replacing it with a new one.
* Important API-Changes(!): Changes in the Changer-Object. A Changer does now need a complete permission node instead of only a part of it. This node can be completely custom and will still be checked on sign creation. Make sure you change that inside your plugins that use this, cause otherwise you will wonder why your players are not able to place signs!!

## v0.8 Release (2012-12-28)
### Supported MC versions: 1.4.6
* Changed everything: This plugin now uses ProtocolLib for modifying the sign packets.

## v0.7 Release (2012-12-19)
### Supported MC versions: 1.4.5
* No changes: Simply renamed the imports for compatibility with craftbukkit 1.4.5 R1.0.

## v0.6 Release (2012-11-09)
### Supported MC versions: 1.4.2
* Added: Changer to the "API"
* Fixed some problem which occurred on reload with version 1.4.1 R0.2

Note: It sometimes displays a wrong error message saying this plugin would cause some error and you should contact the plugin author (me), but in reality these errors are mostly created by other plugins and this message only occurs, because this plugin manipulates packet sending, so it is listed in every error log.. :(

## v0.5 Release (2012-10-20)
### Supported MC versions: 1.3.2
* Added: permissions.

## v0.4 Release (2012-10-13)
### Supported MC versions: 1.3.2
* Fix: Duplicated classes after reload.
* Added: Api. 

## v0.2 Release (2012-09-20)
### Supported MC versions: 1.3.2
* Fix: Text longer than 15 characters.

## v0.1 Release (2012-09-18)
### Supported MC versions: 1.3.2
* First release
