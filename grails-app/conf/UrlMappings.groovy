class UrlMappings {

  static mappings = {
    "/loinc" (resources: 'loinc')
    "/ndc" (resources: 'ndcProduct')
    "/icd9Dx" (resources: 'icd9Dx')
    "/icd9Sg" (resources: 'icd9Sg')

    "/$controller/$action?/$id?(.$format)?" {
      constraints {
        // apply constraints here
      }
    }

    "/"(view: "/index")
    "500"(view: '/error')
  }
}
