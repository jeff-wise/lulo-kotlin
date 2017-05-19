
package lulo.value


import effect.*
import lulo.document.*



/**
 * Value
 */


typealias ValueParser<A> = Eff<ValueError, DocPath, A>



sealed class ValueError

data class UnexpectedType(val expected : DocType, val found : DocType) : ValueError()
data class MissingKey(val key : String) : ValueError()
data class UnknownCase(val caseName : String?) : ValueError()


//fun <A> valueError(valueError : ValueError) : Eff<ValueError, ValueParsePath, A> =
//        Err(valueError, ValueParsePath(listOf(NullNode)))
//
//fun <A> valueResult(valueResult : A) : Eff<ValueError, ValueParsePath, A> =
//        Val(valueResult, ValueParsePath(listOf(NullNode)))



//
//data class ValueParsePath(val nodes : List<ValueParseNode>) : Monoid<ValueParsePath>
//{
//
//    infix fun withNode(node : ValueParseNode) : ValueParsePath =
//        ValueParsePath(this.nodes.plus(node))
//
//
//    override fun mappend(path: ValueParsePath) : ValueParsePath
//    {
//        val nodes = mutableListOf<ValueParseNode>()
//
//        for (node in this.nodes) {
//            when (node) {
//                is KeyNode   -> nodes.add(node)
//                is IndexNode -> nodes.add(node)
//            }
//        }
//
//        for (node in path.nodes) {
//            when (node) {
//                is KeyNode   -> nodes.add(node)
//                is IndexNode -> nodes.add(node)
//            }
//        }
//
//        return ValueParsePath(nodes)
//    }
//
//}
//



//sealed class ValueParseNode : Monoid<ValueParseNode>
//{
//    override fun mappend(x: ValueParseNode): ValueParseNode  = x
//}
//
//
//
//data class IndexNode(val index : Int) : ValueParseNode()
//data class KeyNode(val key : String) : ValueParseNode()
//object NullNode : ValueParseNode()


//fun nodePath(node : ValueParseNode) : ValueParsePath =
//        ValueParsePath(listOf(node))






