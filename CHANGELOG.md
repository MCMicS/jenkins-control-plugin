# Changelog

## [Unreleased]
- \#469 Index out of bounds
- New health icons (Fix \#488 Exception with 2023.2 EAP 232.8453.116)
- \#377: Is there any way to ignore hostname mismatch
- Add second Url to use  as "Jenkins URL" which is set in "Jenkins Location"
- \#490 Unable to save plugin settings in 2023.2 Beta #490
- Build for IDE >= 2023.2
- \#461 \[Cosmetics\] separate "Run/Stop on Jenkins" by splitter
### Breaking
- Remove Jenkins 1 setting. Always use Jenkins version 2
- Remove Crumb Data usage
- Usage of API Token is required.

## [0.13.17]
- \#440 java.lang.IllegalArgumentException: URL is malformed
- \#391 Parameters window too big for screen (Try to Restore last width)
- \#443 [Intellij] Error while loading a workspace
- \#450 Show log of a selected build
- \#406 Need Proxy (use jetbrains setting)

## [0.13.16]
- \#415 Unable to enter parameters
- Show error for trigger builds (Read from Header X-Error)
- Fix Proxy Handling
- \#426 Plugin Failing After IntelliJ Upgrade to 0.13.15 if jenkins is hosted Tomcat or QUery Parameters are not relaxed
- Add Trace Logging for Url calls with `#org.codinjutsu.tools.jenkins:trace`

## [0.13.15-2]
- \#413 0.13.15-2022.2 - still not working with Intellij 2022.3

## [0.13.15]
- maximum timeout increased to 300
- add Support for 2022.3
- \#409 IDEA 2022.3: Access is allowed from event dispatch thread only
- Add "Go to server" to context menu

## [0.13.14]
- \#349 Extend support for Git Parameter. Thanks to @chrisxiao
- 2022.1 API Compatibility
- Change icons for show log actions
- \#251 Enable ActiveChoicesParameter. Rendered as Textfield because missing API.

## [0.13.13]
- \#309 Exception in plugin Jenkins Control
- \#322 Nullpointer if Jenkins if not running
- move classes with module 'com.intellij.modules.vcs' to optional dependency
- \#308 Log streaming should cancelable

## [0.13.12]
- \#280 render DateParameterDefinition as textfield
- 2021.2 API Compatibility
- \#297 NPE on Startup
- \#298 Configure Action for Double Click Build
- \#289 Light service class class org.codinjutsu.tools.jenkins.logic.JenkinsNotifier must be final
- Expand Job Node after Load Build for first time
- \#301 ExtensibleChoiceParameterDefinition support request
- \#159 "Unknown parameter" for "Extensible Choice" parameters
- Reload job if trigger build and paramter exists
- \#275 Please rename password label to "Password/API Token"

## [0.13.11]
- Rename Plugin to 'Jenkins Control' to respect Jetbrains naming convention
- \#84 Handle connection exceptions  
  RSS change: Use Notification system. First failed build will shown as warning in Notification Group 'Jenkins Notification'  instead as balloon over Build Status Summary widget.
- \#270 Multiline String Parameter Turkish Character Problem
- \#240 PT_MULTI_SELECT and PT_CHECKBOX not supported:  
  Render Select items as text input

## [0.13.10]
- \#253 [Feature Request] Log in progress  
  Introduce own tool window for Jenkins logs (can be used in Services View).
- \#181 Change to Dynamic Plugin for 2020.3+

## [0.13.9]
- display build parameters (PR #246)
- auto load builds option in settings (PR #246)
- \#249 PT_BRANCH_TAG is unsupported
- \#247 Plugin 'Jenkins Control Plugin' failed to initialize in Android Studio 4.1

## [0.13.8]
- Initialize Notification on Load
- add About Entry for 2020.1+
- \#231 jobs in folders: collapsed after refresh: Last Expanded and Selections are restored after Restart IDE too.  
  Loaded Builds are not reloaded on restore last state.
- \#197 Show log for individual builds of selected jobs

## [0.13.7]
- \#232 PersistentStringParameter not supported
- \#233 Error overlay over whole of screen if connection is lost.  
  Add Notification group to configure the behaviour in IDE settings.(Settings -> Appearance -> Notifications -> Jenkins Notifications)
- \#234 Can't seem to connect to my jenkins server with the latest version 0.13.6-2020.1
- Fix: regression: remove trailing Slash for URL Validation
- \#230: jobs in folders: job name too long
- \#231: jobs in folders: collapsed after refresh

## [0.13.6]
- \#225 Git Branch Parameter is unsupported
- \#227 Jenkins invalid configuration causes AuthenticationException and UI not respond correctly to this

## [0.13.5]
- \#221 Allow showing all build results in the status bar. Thanks to @nvdweem
- \#222 Separate icon for running builds. Thanks to @nvdweem
- \#217 Show Log not possible for selected Job if self-signed or invalid SSL certificate is used
- \#223 FileParameterDefinition is unsupported

## [0.13.4]
- \#219 How to run a jenkins job with hidden fields?
- \#218 "Show log" on Job without Build leads to error

## [0.13.3]
- add GoTo Allure report
- \#213 NodeParameterDefinition is unsupported
- \#215 Read timed out: Add socket and connection timeout to Configuration (default 10 sec.)

## [0.13.2]
- \#64: NumberFormatException on start of IntelliJ
- \#207: NumberFormatException from Build.createBuildFromRss #207
- \#208: Jenkins.LoadBuilds already registered Exception

## [0.13.1]
- \#202: Upload Patch not work for 2019.3.4+
- \#201: Error reading jenkins.diff file
- \#204: Password Parameter default value ignored

## [0.13.0]
- 2020.1 API Compatibility
- require JBR11 for version 2020.1+
- \#195: NullPointerException when "Show log" is clicked and Job Url is used as Server Address
- \#145: parameter separator plugin problems
- \#80: Support text parameters in builds
- \#190: Trigger Build on Double click
- new Icons: Reuse Idea Icons and use Idea guideline for status color

## [0.12.0]
- \#141: Multibranch pipeline builds not shown
- new Icons: Reuse Idea Icons and use Idea guideline for status color
- \#183: Feature Request: Offer option to use green balls instead of blue ones
- \#185: Error while parsing last Build Date for some Build
- \#110: org.json.simple.JSONObject cannot be cast to java.lang.String
- \#127: Argument for @NotNull parameter 'htmlBody'

## [0.11.1]
- Improve startup: Jenkins Window is show earlier

## [0.11.0]
- IDEA 2019.2+ Support
- [fix] #161: Retry Failed Builds
- [fix] #163: spelling error in config form
- [add] #161: Retry Failed Builds
- [add] #140: Add View Log and Test Results
- [add] #162: Add description text in build param dialog
- [fix] #143: NullPointer when "Sort by Build Status" is active
- [fix] #157: NullPointerException when trying to load builds (IntelliJ Version : 2017.3)
- \#180: Show Test Results As Junit is only available if Plugin dependency 'com.intellij.java' exists
- update JSON.simple to 3.1.0

### Deprecated
- Announcement: Deprecate Builds &lt; 2019.3
- Announcement: Deprecate Jenkins 1 Support

## [0.10.0]
- [fix] #137: Jenkins 2 compatibility
- [fix] #135: NPE when Jenkins has some folder
- [add] #131: View job builds + related actions

## [0.9.7]
- Idea 2016 version
  Version 0.9.6
- Idea 15 version
  Version 0.9.5
- [fix] #105 and #107: empty fields in the RSS JSON breaks the content reading
- [fix] Fix Job parser when property has no parameter

## [0.9.4]
- [fix] Fix major issue on Idea 15

## [0.9.3] (thx Cezary for your great work)
- [update] Improve background task running
- [fix] fix Idea 14 and 15 compatibility
- [fix] Fixed threading issues

## [0.9.2]
- [fix] API issues in idea 13

## [0.9.1]
- [fix] limit version for idea 12 due to some API changes API in idea 13

## [0.9.0]
- [Add] [Patch Parameter Plugin] Notification about build progress (thx Yuri for your nice work)
- [Add] [Patch Parameter Plugin] Action "Create Patch and build on Jenkins" in Changelist's context menu (same than above)
- [Add] [Patch Parameter Plugin] Macros $JobName$ for suffix setting (same than above)
- [Add] [Patch Parameter Plugin] Support of Patch Parameter Plugin (https://wiki.jenkins-ci.org/display/JENKINS/Patch+Parameter+Plugin)
- [Upd] [HttpClient][workaround] Set socket and connection timeout to 10 sec.
- [Upd] #55 If 'display name' of the job is set, use it instead of the 'name' value
- [Fix] EDT thread violation
- [Fix] #54 last selected view is not saved after closing the IDE

## [0.8.0]
- [Add] #43 Support multiple selection for favorites
- [Add] #51 Check the hostname sent by the jenkins server
- [Upd] Improve background loading of jenkins data
- [Upd] Browser: Complete UI refactor
- [Upd] Integrate Rss Reader in the Event Log
- [Upd] Replace Jackson lib by json-simple
- [Upd] Migrate to IntelliJ 11
- [Upd] Color and icons for Darcula theme

## [0.7.0]
- [Upd] UI: Fix Color for new Look&Feel Darcula of IDEA 12
- [Upd] UI: Remove unecessary titled 'job' panel
- [Upd] Configuration: Complete UI Refactor
- [Upd] #38 Password storage is managed by IntelliJ (not backward compatible)
- [Upd] #37 user specific configuration is stored in IWS file instead of IPR file (not backward compatible)
- [Upd] Search: Add UP and DOWN key for key shortcut
- [Upd] Search: Replace matcher by contains
- [Fix] Bug in JSON parsing causes infinite loop

## [0.6.0]
- [Add] Support Cloudbees repository
- [Add] Browser : Favorite Job feature (See README.md on github project)
- [Add] Configuration Panel: when clicking on the Test Connection, if the HTTP code is 401 or 403 then the response body is displayed in a panel
- [Upd] Handling JSON data instead of XML
- [Fix] Regression: NPE when clicking on the Test Connection

## [0.5.4]
- [Add] Job can be sorted by status (fail, unstable, success) - See the new button on the panel
- [Upd] Widget: UI layout modification
- [Fix] IDEA-86137 Thread leak in the EDT
- [Fix] #25 The plugin should compare the configured port and the jenkins server one
- [Fix] #26 Incorrect feedback message when clicking on the 'Test Connection' button
- [Fix] Jenkins Panel disappears when typing CTRL + F4

## [0.5.3]
- [Fix] Widget : UI Issue on Windows and MacOS environment

## [0.5.2]
- [Add] Widget : when clicking on the status icon, a popup is displayed with builds status summary info
- [Add] The plugin starts up with the last selected view
- [Upd] Improvement in handling Exception
- [Upd] Job are loaded in background (with a waiting UI decorator)
- [Fix] The Widget should not be duplicated when opening new Project Window
- [Fix] Build Parameter dialog does not work

## [0.5.1]
- [Fix] Regression : When the server url is not set (or set to a dummy url), the plugin should not try to connect
- [Fix] Browser Panel : OneTouch Expandable should be accessible

## [0.5.0]
- [Add] Job Tree : Search job panel (CTRL + F, F3 to search forward, SHIFT + F3 to search backward, the search feature is cyclic)
- [Add] Jenkins Widget that displays either a red icon when the number of the broken builds > 0 or else a blue icon.
- [Upd] Increase loading performance
- [Fix] Connection issue when Job and Rss autorefresh are both enabled
- [Fix] Configuration Panel: File input fields should have a validator
- [Fix] Configuration Panel: Rename 'Password' to 'Password file'

## [0.4.4]
- [Add] Nested View support (1 level only)
- [Add] Job Tree : Build Description is displayed as tooltip
- [Upd] Plugin SDK Migration to ## [107.587] (IDEA 10.5.2)
- [Fix] Handle Http redirection

## [0.4.3]
- [Fix] Jetbrains plateform version used in Phpstorm IDE makes the plugin crash
- [Fix] Rss toolbar Panel Layout should be fixed (again)

## [0.4.2]
- [Upd] Support other JetBrains product
- [Fix] Unsupported build parameters should not raise an error
- [Fix] Regression in refreshing job
- [fix] Rss toolbar Panel Layout should be fixed
- [Fix] After clearing all rss entries, when clicking on update button, rss entries do not appear on the panel

## [0.4.1]
- [Add] Configuration Panel: Crumb Data can be read from a local file (see explaination on the wiki)
- [Rem] Discover Wizard button does not work well
- [Fix] Bad Implementation of Cross Site Request Forgery Prevention Support makes the plugin crash

## [0.4.0]
- [Fix] Remove HttpClient from package (already embedded in Idea 10) and limit this version to Idea 10.x
- [Fix] Cross Site Request Forgery Prevention Support (see wiki for limitations)
- [Fix] Encoded space character in url

## [0.3.0]
- [Add] Parameterized Builds are supported (see wiki to see limitations)
- [Add] Browser Panel: health icon for each job
- [Update] Configuration Panel :  add Button Wizard which try to resolve security configuration
- [Update] Security Support : Rewrite from scratch with replacing CLI by HttpClient (see wiki for more info)
- [Fix] issue #5 : When clicking on the Jenkins plugin settings button, the Jenkins plugin configuration should be always selected

## [0.2.0]
- [Add] Jenkins Security Support (See https://github.com/dboissier/jenkins-control-plugin/wiki to check how to and see limitations)
- [Add] Settings shortcut on the upper toolbar

## [0.1.0]
- Server configuration
- Jenkins jobs display with autorefreshed job list and view selection
- Job Build runner
- Open Job web page and its last build web page on browser
- Autorefreshed Rss Reader View
- Autorefresh functions can be disabled on the Jenkins Configuration Panel

[Unreleased]: https://github.com/MCMicS/jenkins-control-plugin/compare/v0.13.18-eap...HEAD
[0.13.18-eap]: https://github.com/MCMicS/jenkins-control-plugin/compare/v0.13.17...v0.13.18-eap
[0.13.17]: https://github.com/MCMicS/jenkins-control-plugin/compare/v0.13.16...v0.13.17
[0.13.16]: https://github.com/MCMicS/jenkins-control-plugin/compare/v0.13.15-2...v0.13.16
[0.13.15]: https://github.com/MCMicS/jenkins-control-plugin/compare/v0.13.14...v0.13.15
[0.13.15-2]: https://github.com/MCMicS/jenkins-control-plugin/compare/v0.13.15...v0.13.15-2
[0.13.14]: https://github.com/MCMicS/jenkins-control-plugin/compare/v0.13.13...v0.13.14
[0.13.13]: https://github.com/MCMicS/jenkins-control-plugin/compare/v0.13.12...v0.13.13
[0.13.12]: https://github.com/MCMicS/jenkins-control-plugin/compare/v0.13.11...v0.13.12
[0.13.11]: https://github.com/MCMicS/jenkins-control-plugin/compare/v0.13.10...v0.13.11
[0.13.10]: https://github.com/MCMicS/jenkins-control-plugin/compare/v0.13.9...v0.13.10
[0.13.9]: https://github.com/MCMicS/jenkins-control-plugin/compare/v0.13.8...v0.13.9
[0.13.8]: https://github.com/MCMicS/jenkins-control-plugin/compare/v0.13.7...v0.13.8
[0.13.7]: https://github.com/MCMicS/jenkins-control-plugin/compare/v0.13.6...v0.13.7
[0.13.6]: https://github.com/MCMicS/jenkins-control-plugin/compare/v0.13.5...v0.13.6
[0.13.5]: https://github.com/MCMicS/jenkins-control-plugin/compare/v0.13.4...v0.13.5
[0.13.4]: https://github.com/MCMicS/jenkins-control-plugin/compare/v0.13.3...v0.13.4
[0.13.3]: https://github.com/MCMicS/jenkins-control-plugin/compare/v0.13.2...v0.13.3
[0.13.2]: https://github.com/MCMicS/jenkins-control-plugin/compare/v0.13.1...v0.13.2
[0.13.1]: https://github.com/MCMicS/jenkins-control-plugin/compare/v0.13.0...v0.13.1
[0.13.0]: https://github.com/MCMicS/jenkins-control-plugin/compare/v0.12.0...v0.13.0
[0.12.0]: https://github.com/MCMicS/jenkins-control-plugin/compare/v0.11.1...v0.12.0
[0.11.1]: https://github.com/MCMicS/jenkins-control-plugin/compare/v0.11.0...v0.11.1
[0.11.0]: https://github.com/MCMicS/jenkins-control-plugin/compare/v0.10.0...v0.11.0
[0.10.0]: https://github.com/MCMicS/jenkins-control-plugin/compare/v0.9.7...v0.10.0
[0.9.7]: https://github.com/MCMicS/jenkins-control-plugin/compare/v0.9.4...v0.9.7
[0.9.4]: https://github.com/MCMicS/jenkins-control-plugin/compare/v0.9.3...v0.9.4
[0.9.3]: https://github.com/MCMicS/jenkins-control-plugin/compare/v0.9.2...v0.9.3
[0.9.2]: https://github.com/MCMicS/jenkins-control-plugin/compare/v0.9.1...v0.9.2
[0.9.1]: https://github.com/MCMicS/jenkins-control-plugin/compare/v0.9.0...v0.9.1
[0.9.0]: https://github.com/MCMicS/jenkins-control-plugin/compare/v0.8.0...v0.9.0
[0.8.0]: https://github.com/MCMicS/jenkins-control-plugin/compare/v0.7.0...v0.8.0
[0.7.0]: https://github.com/MCMicS/jenkins-control-plugin/compare/v0.6.0...v0.7.0
[0.6.0]: https://github.com/MCMicS/jenkins-control-plugin/compare/v0.5.4...v0.6.0
[0.5.4]: https://github.com/MCMicS/jenkins-control-plugin/compare/v0.5.3...v0.5.4
[0.5.3]: https://github.com/MCMicS/jenkins-control-plugin/compare/v0.5.2...v0.5.3
[0.5.2]: https://github.com/MCMicS/jenkins-control-plugin/compare/v0.5.1...v0.5.2
[0.5.1]: https://github.com/MCMicS/jenkins-control-plugin/compare/v0.5.0...v0.5.1
[0.5.0]: https://github.com/MCMicS/jenkins-control-plugin/compare/v0.4.4...v0.5.0
[0.4.4]: https://github.com/MCMicS/jenkins-control-plugin/compare/v0.4.3...v0.4.4
[0.4.3]: https://github.com/MCMicS/jenkins-control-plugin/compare/v0.4.2...v0.4.3
[0.4.2]: https://github.com/MCMicS/jenkins-control-plugin/compare/v0.4.1...v0.4.2
[0.4.1]: https://github.com/MCMicS/jenkins-control-plugin/compare/v0.4.0...v0.4.1
[0.4.0]: https://github.com/MCMicS/jenkins-control-plugin/compare/v0.3.0...v0.4.0
[0.3.0]: https://github.com/MCMicS/jenkins-control-plugin/compare/v0.2.0...v0.3.0
[0.2.0]: https://github.com/MCMicS/jenkins-control-plugin/compare/v0.1.0...v0.2.0
[0.1.0]: https://github.com/MCMicS/jenkins-control-plugin/commits/v0.1.0
