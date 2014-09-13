package com.github.rahulsom.maas

import com.github.rahulsom.swaggydoc.SwaggyList
import com.github.rahulsom.swaggydoc.SwaggyShow
import com.wordnik.swagger.annotations.Api
import com.wordnik.swagger.annotations.ApiImplicitParam
import com.wordnik.swagger.annotations.ApiImplicitParams
import com.wordnik.swagger.annotations.ApiOperation
import com.wordnik.swagger.annotations.ApiResponse
import com.wordnik.swagger.annotations.ApiResponses
import grails.converters.JSON
import grails.converters.XML
import grails.plugin.springsecurity.annotation.Secured
import grails.transaction.Transactional
import org.apache.commons.lang.StringUtils
import org.h2.tools.Csv

import java.text.SimpleDateFormat

import static org.springframework.http.HttpStatus.NOT_FOUND

@Transactional(readOnly = true)
@Secured('ROLE_USER')
@Api(value = "Loinc Code",
    produces = 'application/json,application/xml,text/html',
    consumes = 'application/json,application/xml,application/x-www-form-urlencoded'
)
class LoincController {

  Class resource = Loinc
  def elasticSearchService
  static allowedMethods = [save: "POST", ]

  @SwaggyList
  def index() {
    params.max = Math.min(params.max ?: 10, 100)
    if (params.q) {
      def search = Loinc.search(params.q, params)
      def result = new ListResponse(search.total as int, search.searchResults as List<Loinc>)
      respond result, model: [loincInstanceCount: search.total]
    } else {
      def result = new ListResponse(Loinc.count(), Loinc.list(params))
      respond result, model: [loincInstanceCount: Loinc.count()]
    }
  }

  @SwaggyShow
  def show() {
    respond Loinc.get(params.id)
  }

  @Transactional
  @Secured('ROLE_ADMIN')
  @ApiOperation(value = "Save LOINC Codes", response = Void)
  @ApiResponses([
      @ApiResponse(code = 422, message = 'Bad Entity Received'),
  ])
  @ApiImplicitParams([
      @ApiImplicitParam(name = 'file', paramType = 'form', required = true, dataType = 'string'),
  ])
  /**
   * fileLocation - e.g. '/Users/rahulsomasunderam/Downloads/LOINC_248_Text/loinc.csv'
   */
  def save() {
    Loinc.executeUpdate('DELETE from Loinc')

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
    elasticSearchService.unindex(Loinc)
    elasticSearchService.index(Loinc)

    withFormat {
      html {
        render("Complete!")
      }
      json {
        def retval = [status: 'Complete']
        render retval as JSON
      }
      xml {
        def retval = [status: 'Complete']
        render retval as XML
      }
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
