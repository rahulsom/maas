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

/*
if (!shaMatches("data/loinc.sha1", "$dataHome/loinc/loinc.csv")) {
  service.storeLoinc("$dataHome/loinc/loinc.csv")
  updateSha("data/loinc.sha1", "$dataHome/loinc/loinc.csv")
} */

if (!shaMatches("data/CMS32_DESC_LONG_DX.txt.sha1", "$dataHome/icd9/CMS32_DESC_LONG_DX.txt") ||
  !shaMatches("data/CMS32_DESC_SHORT_DX.txt.sha1", "$dataHome/icd9/CMS32_DESC_SHORT_DX.txt")
) {
  service.storeIcd9Dx("$dataHome/icd9/CMS32_DESC_LONG_DX.txt", "$dataHome/icd9/CMS32_DESC_SHORT_DX.txt")
  updateSha("data/CMS32_DESC_LONG_DX.txt.sha1", "$dataHome/icd9/CMS32_DESC_LONG_DX.txt")
  updateSha("data/CMS32_DESC_SHORT_DX.txt.sha1", "$dataHome/icd9/CMS32_DESC_SHORT_DX.txt")
}

if (!shaMatches("data/CMS32_DESC_LONG_SG.txt.sha1", "$dataHome/icd9/CMS32_DESC_LONG_SG.txt") ||
  !shaMatches("data/CMS32_DESC_SHORT_SG.txt.sha1", "$dataHome/icd9/CMS32_DESC_SHORT_SG.txt")
) {
  service.storeIcd9Sg("$dataHome/icd9/CMS32_DESC_LONG_SG.txt", "$dataHome/icd9/CMS32_DESC_SHORT_SG.txt")
  updateSha("data/CMS32_DESC_LONG_SG.txt.sha1", "$dataHome/icd9/CMS32_DESC_LONG_SG.txt")
  updateSha("data/CMS32_DESC_SHORT_SG.txt.sha1", "$dataHome/icd9/CMS32_DESC_SHORT_SG.txt")
}

/*
if (!shaMatches("data/product.sha1", "$dataHome/ndc/product.txt") ||
    !shaMatches("data/package.sha1", "$dataHome/ndc/package.txt")) {
  def errata = [:]
  def badIds = []
  new File("$dataHome/ndc/ndc-diff.txt").eachLine {
    // Packages to left
    // Products to right
    def (packageId, productId) = it.tokenize('|<>')*.trim()
    if (packageId && productId) {
      errata[packageId] = productId
    } else if (packageId) {
      badIds << packageId
    }
  }
  service.storeNdc("$dataHome/ndc/product.txt", "$dataHome/ndc/package.txt", errata, badIds)
  updateSha("data/product.sha1", "$dataHome/ndc/product.txt")
  updateSha("data/package.sha1", "$dataHome/ndc/package.txt")
}          */