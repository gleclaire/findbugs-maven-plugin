#!groovy

// fb2local -f /opt/findbugs/findbugs-2.0.0-rc2 -v 2.0.0-rc2

def findbugsHome = System.getenv("FINDBUGS_HOME")
def antBuilder = new AntBuilder()

def cli = new CliBuilder(usage:'fb2local -f findbugs.home -version version')
cli.h(longOpt: 'help', 'usage information')
cli.f(argName: 'home',  longOpt: 'home', required: false, args: 1, type:GString, 'Findbugs home directory')
cli.v(argName: 'version',  longOpt: 'version', required: true, args: 1, type:GString, 'Findbugs version')

def opt = cli.parse(args)
if (!opt) { return }
if (opt.h) opt.usage()
if (opt.f) findbugsHome = opt.f
def findbugsVersion = opt.v

println "findbugsHome is ${findbugsHome}"
println "findbugsVersion is ${findbugsVersion}"
println "Done parsing"

def cmdPrefix = """"""

println "os.name is " + System.getProperty("os.name")

if (System.getProperty("os.name").toLowerCase().contains("windows")) cmdPrefix = """cmd /c """

def modules = ["annotations", "bcel", "findbugs", "findbugs-ant", "jFormatString", "jsr305" ]

modules.each(){ module ->
  antBuilder.copy(file: new File("${module}.pom"), toFile: new File("${module}.xml"), overwrite: true ) {
    filterset() {
      filter(token: "findbugs.version", value: "${findbugsVersion}")
    }
  }

  cmd = cmdPrefix + """mvn install:install-file -DpomFile=${module}.xml -Dfile=${findbugsHome}/lib/${module}.jar -DgroupId=com.google.code.findbugs -DartifactId=${module} -Dversion=${findbugsVersion} -Dpackaging=jar"""
  proc = cmd.execute()
  println proc.text
}

antBuilder.delete(file: "pom.xml")


