package afterparty

class AfterpartyUser {

    String username
    String password
    boolean enabled
    boolean accountExpired
    boolean accountLocked
    boolean passwordExpired

    static constraints = {
        username blank: false, unique: true
        password blank: false
    }

    static mapping = {
        password column: '`password`'
    }

    static hasMany = [studies : Study]

    Set<AfterPartyRole> getAuthorities() {
        AfterpartyUserAfterPartyRole.findAllByAfterpartyUser(this).collect { it.afterPartyRole } as Set
    }
}
