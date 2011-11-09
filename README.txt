/**********************************************************/
/* Jenkins Control Plugin for IntelliJ IDEA               */
/* ChangeLog :                                            */
/*            - 0.1: Initial version                      */
/*            - 0.2: Add security support (based on CLI)  */
/*            - 0.3:                                      */
/*                   - Rewrite security support (replace  */
/*                     CLI by HttpClient)                 */
/*                   - Add Weather icon on the job        */
/*                   - Parameterized builds are supported */
/*                     (see limitations on the wiki)      */
/**********************************************************/

Description : This plugin allows to view the content of your Jenkins Continous Integration Server.

Plugin Compatibility :

This plugin was built with JDK 1.5 and IntelliJ-8.1.3-jdk5 version. Jenkins 1.437 was use for manual testing.

Installation steps:

Download this plugin from your IDE (Settings -> Plugins -> Available -> Jenkins Control Plugin -> Right Click "Download and Install").
You can also download it from your favorite browser at http://plugins.intellij.net/?idea&id=6110.

Configuration steps:

  -  Click on the Jenkins Settings button (or click on the IDEA General Settings and select Jenkins Control Plugin):
  -  Set your Jenkins url in the Server Address text field.
  -  If you are not sure about the security configuration of your Jenkins, you can click on the 'Wizard' Button.
     (Please note that only In-House Jenkins Security mode and SSL Protocol with trusted certificate are supported)
  -  If security is enabled on the Jenkins Server, put your credentials (Note that the password should be put in a file).
     To make sure your security parameters are correct, you can test by clicking on the 'Test Connection' button.

  -  If you want the plugin to refresh periodically the Jenkins workspace, you can set a timeout value
  -  You can also specify a build start delay (in sec.) that will be set when launching a build.
  -  You can specify a preferred view at Start Up.
  -  Save your configuration by clicking on the OK or Apply Button

Usage:

By default, Jenkins Plugin Panel is available on the right of the IDE. Click on the Jenkins Button to display it.
The panel is divided into 2 parts :
Up part - Job view
    To view the jobs You have to refresh the Jenkins Workspace by right-clicking on the Server icon node
    You can select some view by selecting of them in the combo box.
    When you right click on a job some options are available such as Launch a Build, View The Job's Page and View the Last Build Results.
Down part - Rss View
    To get the last rss feeds, you have to click on the Rss icon button, a list will appear and you will be able to clear each of them or all of them.


Limitations :

This software follows some agile practice: Provide basic functionality and get feedback from the users. So, this version does not cover all needs and it is opened for any suggestion that you can send by email (david [dot] boissier [at] gmail [dot] com).


Thanks :

I would like to thank
- Mark James author of the famfamfam web site who provides beautiful icons.
- Kohsuke Kawaguchi for providing us such a great CI server
- Sébastian Le Merdy for providing support for Eclipse version
- Jetbrains Team for providing us an incredible IDE (certainly the best that Java developers could have).
- Guys from Lex Group : Boris Gonnot, Regis Medina, Sébastien Crego, Olivier Catteau, Jean Baptiste Potonnier and others Agile ninjas.
- My wife and my daughter who support me to have fun in software development and also remind me my husband/father duty ;).
