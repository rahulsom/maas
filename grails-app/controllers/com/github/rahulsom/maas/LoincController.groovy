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
import org.grails.plugins.metrics.groovy.Timed
import org.h2.tools.Csv
import org.hibernate.StatelessSession
import org.hibernate.Transaction

import java.text.SimpleDateFormat

import static org.springframework.http.HttpStatus.NOT_FOUND

@Transactional(readOnly = true)
@Secured('ROLE_USER')
@Api(value = "Loinc Code", description = "LOINC Codes",
    produces = 'application/json,application/hal+json,application/xml,text/html',
    consumes = 'application/json,application/xml,application/x-www-form-urlencoded'
)
class LoincController {

  def elasticSearchService
  def sessionFactory
  static allowedMethods = [save: "POST", ]

  @SwaggyList
  @Timed(name='loincsearch')
  def index() {
    params.max = Math.min(params.max ?: 10, 100)
    if (params.q) {
      def search = Loinc.search(params.q, params)
      respond search.searchResults, model: [loincInstanceCount: search.total]
    } else {
      respond Loinc.list(params), model: [loincInstanceCount: Loinc.count()]
    }
  }

  @SwaggyShow
  @Timed(name='loincshow')
  def show() {
    respond Loinc.get(params.id)
  }

  @Secured('ROLE_ADMIN')
  @ApiOperation(value = "Save LOINC Codes", response = Void)
  @ApiResponses([
      @ApiResponse(code = 422, message = 'Bad Entity Received'),
  ])
  @ApiImplicitParams([
      @ApiImplicitParam(name = 'file', paramType = 'form', required = true, dataType = 'string',
          value="CSV File from downloaded zip. E.g. '/opt/loinc/loinc.csv'"),
  ])
  def save() {
    StatelessSession session = sessionFactory.openStatelessSession()
    Transaction tx = session.beginTransaction()

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
    int batchSize = 0
    long lastCheck = System.nanoTime()
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
      // loinc.save(flush: ++batchSize % 200 == 0)
      session.insert(loinc)
      if (++batchSize %200 == 0) {
        long newCheck = System.nanoTime()
        println "${batchSize} loincs down in ${(newCheck - lastCheck)/1000000.0} ms"
        lastCheck = newCheck
      }

    }

    tx.commit()
    session.close()

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
