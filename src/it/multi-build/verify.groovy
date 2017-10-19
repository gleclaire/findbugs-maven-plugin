/*
 * Copyright (C) 2006-2017 the original author or authors.
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

File findbugsHtml =  new File(basedir, "modules/${module}/target/site/findbugs.html")
assert findbugsHtml.exists()

File findbugXdoc = new File(basedir, "modules/${module}/target/findbugs.xml")
assert findbugXdoc.exists()

File findbugXml = new File(basedir, "modules/${module}/target/findbugsXml.xml")
assert findbugXml.exists()


println '***************************'
println "Checking HTML file"
println '***************************'


def xhtmlParser = new XmlSlurper();
xhtmlParser.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false)
xhtmlParser.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
def path = xhtmlParser.parse( findbugsHtml )

//*[@id="contentBox"]/div[2]/table/tbody/tr[2]/td[2]
def findbugsErrors = path.body.'**'.find {div -> div.@id == 'contentBox'}.div[1].table.tr[1].td[1].toInteger()
println "Error Count is ${findbugsErrors}"

println '***************************'
println "Checking xDoc file"
println '***************************'

path = new XmlSlurper().parse(findbugXdoc)

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

path = new XmlSlurper().parse(findbugXml)

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

findbugsHtml =  new File(basedir, "modules/${module}/target/site/findbugs.html")
assert findbugsHtml.exists()

findbugXdoc = new File(basedir, "modules/${module}/target/findbugs.xml")
assert findbugXdoc.exists()

findbugXml = new File(basedir, "modules/${module}/target/findbugsXml.xml")
assert findbugXml.exists()


println '***************************'
println "Checking HTML file"
println '***************************'

path = new XmlSlurper(true, true, true).parse( findbugsHtml )
//*[@id="contentBox"]/div[2]/table/tbody/tr[2]/td[2]
findbugsErrors = path.body.'**'.find {div -> div.@id == 'contentBox'}.div[1].table.tr[1].td[1].toInteger()
println "Error Count is ${findbugsErrors}"

println '***************************'
println "Checking xDoc file"
println '***************************'

path = new XmlSlurper().parse(findbugXdoc)

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

path = new XmlSlurper().parse(findbugXml)

allNodes = path.depthFirst().collect{ it }
findbugsXmlErrors = allNodes.findAll {it.name() == 'BugInstance'}.size()
println "BugInstance size is ${findbugsXmlErrors}"

assert findbugsErrors == findbugsXmlErrors

findbugsXmlErrors = allNodes.findAll {it.name() == 'BugInstance'  && it.@type == "DLS_DEAD_LOCAL_STORE"}.size()
println "BugInstance with includes size is ${findbugsXmlErrors}"

assert findbugsErrors == findbugsXmlErrors

