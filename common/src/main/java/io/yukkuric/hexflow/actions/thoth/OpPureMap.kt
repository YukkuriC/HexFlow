package io.yukkuric.hexflow.actions.thoth

import at.petrak.hexcasting.api.casting.SpellList
import io.yukkuric.hexflow.actions.base.AbstractThoth

// (comment_polluted)splat,(duplicate,bool_coerce,(stack_len,last_n_list,halt)unappend,if,eval)(1,1,4,5,null,4)pure_map
object OpPureMap : AbstractThoth() {
    override fun doThoth(code: SpellList, data: SpellList) = resultPureThoth(code, data)
}