package com.github.rahulsom.maas

import grails.plugin.springsecurity.annotation.Secured
import grails.transaction.Transactional
import org.apache.commons.lang.StringUtils
import org.h2.tools.Csv

import java.text.SimpleDateFormat

import static org.springframework.http.HttpStatus.*

@Transactional(readOnly = true)
@Secured('ROLE_USER')
class LoincController {

  def elasticSearchService
  static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

  def index(Integer max) {
    params.max = Math.min(max ?: 10, 100)
    if (params.q) {
      def search = Loinc.search(params.q, params)
      def result = new ListResponse(search.total as int, search.searchResults as List<Loinc>)
      respond result, model: [loincInstanceCount: search.total]
    } else {
      def result = new ListResponse(Loinc.count(), Loinc.list(params))
      respond result, model: [loincInstanceCount: Loinc.count()]
    }
  }

  def show(Loinc loincInstance) {
    respond loincInstance
  }

  @Transactional
  @Secured('ROLE_ADMIN')
  /**
   * fileLocation - e.g. '/Users/rahulsomasunderam/Downloads/LOINC_248_Text/loinc.csv'
   */
  def save() {
    elasticSearchService.unindex(Loinc)

    def rs = new Csv().read(new FileReader(params.file), null)
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

    elasticSearchService.index(Loinc)

    request.withFormat {
      form multipartForm {
        flash.message = message(code: 'default.created.message', args: [message(code: 'loinc.label', default: 'Loinc')])
      }
      '*' { respond 'Complete', [status: CREATED] }
    }
  }

  protected void notFound() {
    request.withFormat {
      form multipartForm {
        flash.message = message(code: 'default.not.found.message', args: [message(code: 'loinc.label', default: 'Loinc'), params.id])
        redirect action: "index", method: "GET"
      }
      '*' { render status: NOT_FOUND }
    }
  }
}
