#!/usr/bin/env groovy

def cli = new CliBuilder(usage:'fb2bundle -f findbugs.home -version version')
cli.h(longOpt: 'help', 'usage information')
cli.v(argName: 'version',  longOpt: 'version', required: true, args: 1, type:GString, 'Findbugs version')

def opt = cli.parse(args)
if (!opt) { return }
if (opt.h) opt.usage()
def findbugsVersion = opt.v

println "findbugsVersion is ${findbugsVersion}"
println "Done parsing"

def cmdPrefix = """"""

println "os.name is " + System.getProperty("os.name")

if (System.getProperty("os.name").toLowerCase().contains("windows")) cmdPrefix = """cmd /c """

def modules = ["annotations", "bcel", "findbugs", "findbugs-ant", "jFormatString", "jsr305" ]

modules.each(){ module ->
    println "Processing ${module}........"
    cmd = cmdPrefix + """mvn repository:bundle-pack -B -DgroupId=com.google.code.findbugs -DartifactId=${module} -Dversion=${findbugsVersion}"""
    proc = cmd.execute()
    println proc.text
}

