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
import org.grails.plugins.metrics.groovy.Timed
import org.h2.tools.Csv
import org.hibernate.StatelessSession
import org.hibernate.Transaction

import java.text.SimpleDateFormat

import static org.springframework.http.HttpStatus.NOT_FOUND

@Transactional(readOnly = true)
@Secured('ROLE_USER')
@Api(value = "NDC Code", description = "NDC Codes",
    produces = 'application/json,application/hal+json,application/xml,text/html',
    consumes = 'application/json,application/xml,application/x-www-form-urlencoded'
)

class NdcProductController {

  def dataService
  static allowedMethods = [save: "POST", ]

  @SwaggyList
  @Timed(name='ndcsearch')
  def index() {
    params.max = Math.min(params.int('max') ?: 10, 100)
    params.offset =  params.int('offset') ?: 0
    if (params.q) {
      params.from = params.offset
      params.size = params.max
      def search = NdcProduct.search(params.q, params)
      respond search.searchResults , model: [ndcProductInstanceCount: search.total]
    } else {
      respond NdcProduct.list(params), model: [ndcProductInstanceCount: NdcProduct.count()]
    }
  }

  @SwaggyShow
  @Timed(name='ndcshow')
  def show() {
    respond NdcProduct.get(params.id)
  }

  @Secured('ROLE_ADMIN')
  @ApiOperation(value = "Save NDC Codes", response = Void)
  @ApiResponses([
      @ApiResponse(code = 422, message = 'Bad Entity Received'),
  ])
  @ApiImplicitParams([
      @ApiImplicitParam(name = 'package', paramType = 'form', required = true, dataType = 'string',
          value="CSV File from downloaded zip. E.g. '/opt/ndc/package.txt'"),
      @ApiImplicitParam(name = 'product', paramType = 'form', required = true, dataType = 'string',
          value="CSV File from downloaded zip. E.g. '/opt/ndc/product.txt'"),
  ])
  def save() {
    def productFile = params.product
    def packageFile = params.package

    dataService.storeNdc(productFile, packageFile, errata, badIds)

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
        flash.message = message(code: 'default.not.found.message', args: [message(code: 'ndcproduct.label', default: 'Ndc Product'), params.id])
        redirect action: "index", method: "GET"
      }
      '*' { render status: NOT_FOUND }
    }
  }

  static errata = [
  ]

  static badIds = [
      "0054-8722_645709c0-48d1-444f-8d51-f38b7109ecf3"
  ]
}
