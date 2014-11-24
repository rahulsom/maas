import com.github.rahulsom.maas.NdcProductController

def env = System.getenv()
def service = ctx.getBean('dataService')
def dataHome = env['DATADIR'] ?: "/opt"

boolean shaMatches(String shaName, String fileName) {
  def shaFile = new File(shaName)
  if (!shaFile.exists()) {
    return false
  }
  def fileSha = shaFile.text
  def trueSha = "sha1sum $fileName | cut -d ' ' -f 1".execute().inputStream.text.split(' ')[0]
  return fileSha == trueSha
}

def updateSha(String shaName, String fileName) {
  def shaFile = new File(shaName)
  def trueSha = "sha1sum $fileName | cut -d ' ' -f 1".execute().inputStream.text.split(' ')[0]
  shaFile.text = trueSha
}

if (!shaMatches("data/loinc.sha1", "$dataHome/loinc/loinc.csv")) {
  service.storeLoinc("$dataHome/loinc/loinc.csv")
  updateSha("data/loinc.sha1", "$dataHome/loinc/loinc.csv")
}
if (!shaMatches("data/product.sha1", "$dataHome/ndc/product.txt") ||
    !shaMatches("data/package.sha1", "$dataHome/ndc/package.txt")) {
  service.storeNdc("$dataHome/ndc/product.txt", "$dataHome/ndc/package.txt", NdcProductController.errata, NdcProductController.badIds)
  updateSha("data/product.sha1", "$dataHome/ndc/product.txt")
  updateSha("data/package.sha1", "$dataHome/ndc/package.txt")
}
