import com.github.rahulsom.maas.Loinc
import org.apache.commons.lang.StringUtils
import org.h2.tools.Csv

import java.text.SimpleDateFormat

class BootStrap {

  def init = { servletContext ->
    Loinc.withSession { session ->

      def rs = new Csv().read(new FileReader('/Users/rahulsomasunderam/Downloads/LOINC_248_Text/loinc.csv'), null)
      def rsm = rs.metaData
      def fieldNameMap = (1..(rsm.columnCount)).collect().collectEntries { i ->
        def colName = rsm.getColumnName(i)
        def fieldName = StringUtils.uncapitalize(colName.split('_').collect {
          StringUtils.capitalize(it.toLowerCase())
        }.join(''))
        if (fieldName == 'class') {
          fieldName += '_'
        }
        [colName, fieldName]
      }
      Loinc loinc = null
      while (rs.next()) {
        loinc = new Loinc()
        fieldNameMap.each { String k, String v ->
          if (loinc.metaClass.properties.find { it.name == v }.type.isAssignableFrom(String)) {
            loinc[v] = rs.getString(k)
          } else if (loinc.metaClass.properties.find { it.name == v }.type.isAssignableFrom(Integer)) {
            loinc[v] = rs.getInt(k)
          } else if (loinc.metaClass.properties.find { it.name == v }.type.isAssignableFrom(Date)) {
            def strVal = rs.getString(k)
            if (strVal) {
              loinc[v] = new SimpleDateFormat('yyyyMMdd').parse(strVal)
            }
          } else {
            println "Unhandled field type: ${loinc.metaClass.properties[v].class}"
          }
        }
        loinc.save()
      }

      loinc.save(flush: true, failOnError: true)
    }
  }
  def destroy = {
  }
}
