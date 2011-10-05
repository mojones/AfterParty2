package afterparty

class AfterPartyTagLib {
    def truncate = { attrs, body ->
        if (body().length() > attrs.maxlength.toInteger()) {
            out << body()[0..attrs.maxlength.toInteger() - 1] + '...'
        }
        else {
            out << body()
        }
    }

}
