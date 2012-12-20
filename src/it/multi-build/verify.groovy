/*
 * Copyright (C) 2006-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */



//  check module 1

println '***************************'
println "Checking Module-1"
println '***************************'

def module = "module-1"

assert new File(basedir, "modules/${module}/target/site/findbugs.html").exists()

assert new File(basedir, "modules/${module}/target/findbugs.xml").exists()

assert new File(basedir, "modules/${module}/target/findbugsXml.xml").exists()



def xmlSlurper = new XmlSlurper()
xmlSlurper.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
xmlSlurper.setFeature("http://xml.org/sax/features/namespaces", false)

def path = xmlSlurper.parse( new File(basedir, "modules/${module}/target/site/findbugs.html" ) )

println '***************************'
println "Checking HTML file"
println '***************************'

//def bugNodes = path.body.div.findAll {it.@id == 'bodyColumn'}.div[1].table.tr[1].td[1]  //.div.table.tr.td
//println "bugNodes value is ${bugNodes.toInteger()}"
def findbugsErrors = path.body.div.findAll {it.@id == 'bodyColumn'}.div[1].table.tr[1].td[1].toInteger()
println "Error Count is ${findbugsErrors}"

println '***************************'
println "Checking xDoc file"
println '***************************'

path = new XmlSlurper().parse(new File(basedir, "modules/${module}/target/findbugs.xml"))

allNodes = path.depthFirst().collect{ it }
def xdocErrors = allNodes.findAll {it.name() == 'BugInstance'}.size()
println "BugInstance size is ${xdocErrors}"

assert findbugsErrors == xdocErrors

xdocErrors = allNodes.findAll {it.name() == 'BugInstance'  && it.@type == "DLS_DEAD_LOCAL_STORE"}.size()
println "BugInstance with includes size is ${xdocErrors}"

assert findbugsErrors == xdocErrors

println '**********************************'
println "Checking Findbugs Native XML file"
println '**********************************'

path = new XmlSlurper().parse(new File(basedir, "modules/${module}/target/findbugsXml.xml"))

allNodes = path.depthFirst().collect{ it }
def findbugsXmlErrors = allNodes.findAll {it.name() == 'BugInstance'}.size()
println "BugInstance size is ${findbugsXmlErrors}"

assert findbugsErrors == findbugsXmlErrors

findbugsXmlErrors = allNodes.findAll {it.name() == 'BugInstance'  && it.@type == "DLS_DEAD_LOCAL_STORE"}.size()
println "BugInstance with includes size is ${findbugsXmlErrors}"

assert findbugsErrors == findbugsXmlErrors





//  check module 2

println '***************************'
println "Checking Module-2"
println '***************************'

module = "module-2"

assert new File(basedir, "modules/${module}/target/site/findbugs.html").exists()

assert new File(basedir, "modules/${module}/target/findbugs.xml").exists()

assert new File(basedir, "modules/${module}/target/findbugsXml.xml").exists()


xmlSlurper = new XmlSlurper()
xmlSlurper.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
xmlSlurper.setFeature("http://xml.org/sax/features/namespaces", false)

path = xmlSlurper.parse( new File(basedir, "modules/${module}/target/site/findbugs.html" ) )

println '***************************'
println "Checking HTML file"
println '***************************'

//def bugNodes = path.body.div.findAll {it.@id == 'bodyColumn'}.div[1].table.tr[1].td[1]  //.div.table.tr.td
//println "bugNodes value is ${bugNodes.toInteger()}"
findbugsErrors = path.body.div.findAll {it.@id == 'bodyColumn'}.div[1].table.tr[1].td[1].toInteger()
println "Error Count is ${findbugsErrors}"

println '***************************'
println "Checking xDoc file"
println '***************************'

path = new XmlSlurper().parse(new File(basedir, "modules/${module}/target/findbugs.xml"))

allNodes = path.depthFirst().collect{ it }
xdocErrors = allNodes.findAll {it.name() == 'BugInstance'}.size()
println "BugInstance size is ${xdocErrors}"

assert findbugsErrors == xdocErrors

xdocErrors = allNodes.findAll {it.name() == 'BugInstance'  && it.@type == "DLS_DEAD_LOCAL_STORE"}.size()
println "BugInstance with includes size is ${xdocErrors}"

assert findbugsErrors == xdocErrors

println '**********************************'
println "Checking Findbugs Native XML file"
println '**********************************'

path = new XmlSlurper().parse(new File(basedir, "modules/${module}/target/findbugsXml.xml"))

allNodes = path.depthFirst().collect{ it }
findbugsXmlErrors = allNodes.findAll {it.name() == 'BugInstance'}.size()
println "BugInstance size is ${findbugsXmlErrors}"

assert findbugsErrors == findbugsXmlErrors

findbugsXmlErrors = allNodes.findAll {it.name() == 'BugInstance'  && it.@type == "DLS_DEAD_LOCAL_STORE"}.size()
println "BugInstance with includes size is ${findbugsXmlErrors}"

assert findbugsErrors == findbugsXmlErrors

