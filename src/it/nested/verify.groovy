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

assert new File(basedir, 'target/site/findbugs.html').exists()

assert new File(basedir, 'target/findbugs.xml').exists()

assert new File(basedir, 'target/findbugsXml.xml').exists()


def xmlSlurper = new XmlSlurper()
xmlSlurper.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
xmlSlurper.setFeature("http://xml.org/sax/features/namespaces", false)

def path = xmlSlurper.parse( new File(basedir, 'target/site/findbugs.html') )

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

path = new XmlSlurper().parse(new File(basedir, 'target/findbugs.xml'))

allNodes = path.depthFirst().collect{ it }
def xdocErrors = allNodes.findAll {it.name() == 'BugInstance'}.size()
println "BugInstance size is ${xdocErrors}"


println '**********************************'
println "Checking Findbugs Native XML file"
println '**********************************'

path = new XmlSlurper().parse(new File(basedir, 'target/findbugsXml.xml'))

allNodes = path.depthFirst().collect{ it }
def findbugsXmlErrors = allNodes.findAll {it.name() == 'BugInstance'}.size()
println "BugInstance size is ${findbugsXmlErrors}"

assert xdocErrors == findbugsXmlErrors
assert findbugsErrors == findbugsXmlErrors


