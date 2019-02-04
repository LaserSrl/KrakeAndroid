package com.krake.core.component.annotation

import android.os.Bundle
import com.krake.core.component.base.ComponentManager
import com.krake.core.component.base.ComponentModule

/**
 * Per ogni campo annotato con l'annotation [BundleResolvable], verrà istanziato il [ComponentModule] corrispondente e
 * successivamente verranno letti i valori da un [Bundle].
 * L'annotation è applicabile solo ai campi public e di tipo [ComponentModule].
 *
 * @see ComponentManager.resolveBundle
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class BundleResolvable