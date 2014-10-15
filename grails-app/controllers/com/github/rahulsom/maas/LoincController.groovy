package com.github.rahulsom.maas

import com.github.rahulsom.swaggydoc.SwaggyList
import com.github.rahulsom.swaggydoc.SwaggyShow
import com.wordnik.swagger.annotations.*
import grails.converters.JSON
import grails.converters.XML
import grails.plugin.springsecurity.annotation.Secured
import grails.transaction.Transactional
import org.grails.plugins.metrics.groovy.Timed

import static org.springframework.http.HttpStatus.NOT_FOUND

@Transactional(readOnly = true)
@Secured('ROLE_USER')
@Api(value = "Loinc Code", description = "LOINC Codes",
    produces = 'application/json,application/hal+json,application/xml,text/html',
    consumes = 'application/json,application/xml,application/x-www-form-urlencoded'
)
class LoincController {

  def dataService
  static allowedMethods = [save: "POST", ]

  @SwaggyList
  @Timed(name='loincsearch')
  def index() {
    params.max = Math.min(params.int('max') ?: 10, 100)
    params.offset =  params.int('offset') ?: 0
    if (params.q) {
      params.from = params.offset
      params.size = params.max
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

    dataService.storeLoinc(params.file)

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
