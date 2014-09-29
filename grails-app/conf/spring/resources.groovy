import com.github.rahulsom.maas.Loinc
import com.github.rahulsom.maas.NdcProduct
import grails.rest.render.hal.HalJsonCollectionRenderer
import grails.rest.render.hal.HalJsonRenderer

// Place your Spring DSL code here
beans = {
  halNdcRenderer(HalJsonRenderer, NdcProduct)
  halNdcListRenderer(HalJsonCollectionRenderer, NdcProduct)

  halLoincRenderer(HalJsonRenderer, Loinc)
  halLoincListRenderer(HalJsonCollectionRenderer, Loinc)
}