import com.github.rahulsom.maas.NdcProductController

def service = ctx.getBean('dataService')
def dataHome = "/Users/rahul/data"
service.storeLoinc("$dataHome/loinc/loinc.csv")
service.storeNdc("$dataHome/ndc/product.txt", "$dataHome/ndc/package.txt", NdcProductController.errata, NdcProductController.badIds)
