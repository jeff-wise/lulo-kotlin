
package lulo.value

import effect.Eff
import effect.Err
import effect.Identity
import lulo.document.DocType


/**
 * Value
 */


typealias ValueParser<A> = Eff<ValueError, Identity, A>



sealed class ValueError

data class UnexpectedType(val expected : DocType, val found : DocType) : ValueError()


fun <A> valueError(valueError : ValueError) : Eff<ValueError, Identity, A> =
        Err(valueError, Identity())


