package BugBug.androidApp.data.remote

import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

@Root(name = "Metall", strict = false)
data class GoldResponse(
    @field:ElementList(name = "Record", inline = true, required = false)
    var records: MutableList<Record> = mutableListOf()
)

@Root(name = "Record", strict = false)
data class Record(
    @field:Attribute(name = "Code", required = false)
    var code: String = "",
    @field:Element(name = "Buy", required = false)
    var buy: String = "0"
)