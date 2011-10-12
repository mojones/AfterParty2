package afterparty

class UpdateController {

    // TODO make this more robust
    def updateField = {
        println "updating $params.className with $params.update_value for $params.fieldName"
        Class clazz = grailsApplication.domainClasses.find { it.clazz.simpleName == params.className }.clazz
        clazz.get(params.id).setProperty(params.fieldName, params.update_value)
        render params.update_value
    }

}
