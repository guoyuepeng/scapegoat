package com.sksamuel.scapegoat.inspections.string

import com.sksamuel.scapegoat._

/** @author Zack Grannan */
class UnsafeStringContains extends Inspection("Unsafe string contains", Levels.Error) {

  def inspector(context: InspectionContext): Inspector = new Inspector(context) {
    override def postTyperTraverser = Some apply new context.Traverser {

      import context.global._
      import treeInfo.Applied

      private val Contains = TermName("contains")

      private def isChar(tree: Tree) = tree.tpe.widen.baseClasses.contains(typeOf[Char].typeSymbol)

      private def isString(tree: Tree): Boolean = {
        tree.tpe.widen.baseClasses.contains(typeOf[String].typeSymbol) || (tree match {
          case Apply(left, _) => left.symbol.fullName == "scala.Predef.augmentString"
          case _ => false
        })
      }

      private def isCompatibleType(value: Tree) = isString(value) || isChar(value)

      override def inspect(tree: Tree): Unit = tree match {
        case Applied(Select(lhs, Contains), _, (arg :: Nil) :: Nil) if isString(lhs) && !isCompatibleType(arg) =>
          context.warn(tree.pos, self, tree.toString().take(300))
        case _ =>
          continue(tree)
      }
    }
  }
}

