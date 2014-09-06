import com.github.rahulsom.maas.Loinc
import com.github.rahulsom.maas.auth.Role
import com.github.rahulsom.maas.auth.User
import com.github.rahulsom.maas.auth.UserRole
import org.apache.commons.lang.StringUtils
import org.h2.tools.Csv

import java.text.SimpleDateFormat

class BootStrap {

  def elasticSearchService

  def init = { servletContext ->
    elasticSearchService.unindex()

    User.withNewTransaction {
      def adminRole = new Role(authority: 'ROLE_ADMIN').save(flush: true)
      def userRole = new Role(authority: 'ROLE_USER').save(flush: true)

      def testUser = new User(username: 'admin', password: 'admin')
      testUser.save(flush: true)

      UserRole.create testUser, adminRole, true


    }
    Loinc.withNewTransaction { session ->

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
          if (v == 'loincNum') {
            loinc.id = rs.getString(k)
          } else if (loinc.metaClass.properties.find { it.name == v }.type.isAssignableFrom(String)) {
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
    elasticSearchService.index(Loinc)
  }
  def destroy = {
  }
}
