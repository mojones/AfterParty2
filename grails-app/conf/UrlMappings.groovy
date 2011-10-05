class UrlMappings {

	static mappings = {
		"/$controller/$action?/$id?"{
			constraints {
				// apply constraints here
			}
		}

		"/" {
            controller = "study"
        }
		"500"(view:'/error')
	}
}
