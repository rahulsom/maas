import com.github.rahulsom.maas.NdcProductController

def service = ctx.getBean('dataService')
def dataHome = "/opt"
if (new File("data/loinc.sha1").text != "sha1sum $dataHome/loinc/loinc.csv".execute().inputStream.text) {
  service.storeLoinc("$dataHome/loinc/loinc.csv")
  "sha1sum $dataHome/loinc/loinc.csv > data/loinc.sha1".execute()
}
if (new File("data/product.sha1").text != "sha1sum $dataHome/ndc/product.txt".execute().inputStream.text ||
    new File("data/package.sha1").text != "sha1sum $dataHome/ndc/package.txt".execute().inputStream.text
) {
  service.storeNdc("$dataHome/ndc/product.txt", "$dataHome/ndc/package.txt", NdcProductController.errata, NdcProductController.badIds)
  "sha1sum $dataHome/ndc/package.txt > data/package.sha1".execute()
  "sha1sum $dataHome/ndc/product.txt > data/product.sha1".execute()
}
