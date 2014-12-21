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
import org.grails.plugins.metrics.groovy.Timed

import static org.springframework.http.HttpStatus.*
import grails.transaction.Transactional

@Transactional(readOnly = true)
@Secured('ROLE_USER')
@Api(value = "ICD 9 Procedure", description = "ICD 9 Procedures",
    produces = 'application/json,application/hal+json,application/xml,text/html',
    consumes = 'application/json,application/xml,application/x-www-form-urlencoded'
)
class Icd9SgController {

  def dataService
  static allowedMethods = [save: "POST"]

  @SwaggyList
  @Timed(name='icd9sgsearch')
  def index() {
    params.max = Math.min(params.int('max') ?: 10, 100)
    params.offset =  params.int('offset') ?: 0
    if (params.q) {
      params.from = params.offset
      params.size = params.max
      def search = Icd9Sg.search(params.q, params)
      respond search.searchResults, model: [icd9SgInstanceCount: search.total]
    } else {
      respond Icd9Sg.list(params), model: [icd9SgInstanceCount: Icd9Sg.count()]
    }
  }

  @SwaggyShow
  @Timed(name='icd9sgshow')
  def show(Icd9Sg icd9SgInstance) {
    respond icd9SgInstance
  }

  @Secured('ROLE_ADMIN')
  @ApiOperation(value = "Save ICD9 Procedure Codes", response = Void)
  @ApiResponses([
      @ApiResponse(code = 422, message = 'Bad Entity Received'),
  ])
  @ApiImplicitParams([
      @ApiImplicitParam(name = 'shortFile', paramType = 'form', required = true, dataType = 'string',
          value="Flat File from downloaded zip. E.g. '/opt/icd9/CMS32_DESC_SHORT_SG.txt'"),
      @ApiImplicitParam(name = 'longFile', paramType = 'form', required = true, dataType = 'string',
          value="Flat File from downloaded zip. E.g. '/opt/icd9/CMS32_DESC_LONG_SG.txt'"),
  ])
  def save() {
    dataService.storeIcd9Sg(params.longFile, params.shortFile)

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
        flash.message = message(code: 'default.not.found.message', args: [message(code: 'icd9Sg.label', default: 'Icd9Sg'), params.id])
        redirect action: "index", method: "GET"
      }
      '*' { render status: NOT_FOUND }
    }
  }
}
