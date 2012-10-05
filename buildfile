VERSION_NUMBER = '0.6.0-SNAPSHOT'
PLUGIN_NAME = 'jenkins-control-plugin'
GROUP = 'org.codinjutsu.tools.jenkins'

INTELLIJ_SDK_VERSION = '10.5.2'
INTELLIJ_SDK_NAME = '107.587'
INTELLIJ_SDK_PATH = '/home/avsp/github/ideaIU-10.5.2/lib'

USER = 'avsp'

repositories.remote << 'http://maven01b.int33.cvf:8081/nexus/content/groups/public'
repositories.remote << 'http://repo1.maven.org/maven2'
repositories.remote << 'http://www.ibiblio.org/maven2'
repositories.remote << 'http://mirrors.ibiblio.org/pub/mirrors/maven2'
repositories.remote << 'http://download.java.net/maven/2/'

desc 'Jenkins Plugin for Jetbrains products'
define 'jenkins-control-plugin' do

  project.version = VERSION_NUMBER
  project.group = GROUP

  repositories.local = "/home/#{USER}/.m2/repository"

  manifest['Implementation-Version'] = project.version

  compile.using :target => '1.6', :other => ['-encoding', 'UTF-8'], :deprecation => false
  test.using :java_args => ['-Xmx256m', '-Xms128m'], :fork => 'once'

  # Compile dependencies
  COMMONS_HTTP_CLIENT = 'commons-httpclient:commons-httpclient:jar:3.1'
  COMMONS_LANG = 'commons-lang:commons-lang:jar:2.4'
  COMMONS_LOGGING = 'commons-logging:commons-logging:jar:1.0.4'
  COMMONS_CODEC = 'commons-codec:commons-codec:jar:1.2'
  COMMONS_IO = 'commons-io:commons-io:jar:1.4'

  JSON = 'com.googlecode.json-simple:json-simple:jar:1.1'
  JDOM = 'jdom:jdom:jar:1.0'

  LOG4J = 'log4j:log4j:jar:1.2.14'

  FORMS_RT = "com.intellij:forms_rt:jar:#{INTELLIJ_SDK_VERSION}"
  IDEA_OPEN_API = "com.intellij:idea-openapi:jar:#{INTELLIJ_SDK_VERSION}"

  # Test dependencies
  JUNIT = 'junit:junit:jar:4.8'
  UNITILS = 'org.unitils:unitils-core:jar:3.3'
  MOCKITO = 'org.mockito:mockito-all:jar:1.8.5'
  UISPEC4J = 'org.uispec4j:uispec4j:jdk16:jar:2.4'

  compile.with COMMONS_LANG, LOG4J, JDOM, JSON, FORMS_RT, IDEA_OPEN_API, COMMONS_HTTP_CLIENT, COMMONS_IO
  test.with JUNIT, MOCKITO, UISPEC4J, UNITILS

  jenkins = package :jar

  distribution = package :zip, :id => PLUGIN_NAME, :version => VERSION_NUMBER
  distribution.path('/lib').include jenkins, JSON, COMMONS_LOGGING, COMMONS_IO, COMMONS_CODEC
  distribution.path('/').include(['README.md', 'CHANGELOG.txt', 'LICENSE.txt'])

end