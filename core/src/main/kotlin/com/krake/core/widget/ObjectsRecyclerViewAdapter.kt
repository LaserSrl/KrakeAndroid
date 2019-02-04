package com.krake.core.widget

import android.content.Context
import android.support.annotation.CallSuper
import android.support.annotation.LayoutRes
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import java.lang.ref.WeakReference
import java.lang.reflect.Constructor

/**
 * Created by joel on 17/03/17.
 *  @constructor
 *  @param context context di uso
 *  @param holderClass classe dell'holder deve avere un costruttore pubblico che accetta una classe View
 *  @param objects oggetti da mostrare nella lista. Default null
 *  @param layoutResource risorsa di base delle view per l'holder
 */
abstract class ObjectsRecyclerViewAdapter<ObjectClass, ViewHolderClass : RecyclerView.ViewHolder>(
        context: Context,
        @param:LayoutRes private val layoutResource: Int,
        objects: List<ObjectClass>? = null,
        holderClass: Class<ViewHolderClass>)
    : RecyclerView.Adapter<ViewHolderClass>() {

    private val contextRef: WeakReference<Context> = WeakReference(context)

    val items = mutableListOf<ObjectClass>()

    protected val context: Context? get() = contextRef.get()

    private val viewHolderClassConstructor: Constructor<ViewHolderClass>

    protected val inflater: LayoutInflater

    open var defaultClickReceiver: ClickReceiver<ObjectClass>? = null
    open var defaultLongClickReceiver: LongClickReceiver<ObjectClass>? = null

    init {
        objects?.let {
            items.addAll(it)
        }
        inflater = LayoutInflater.from(context)
        try {
            viewHolderClassConstructor = holderClass.getConstructor(View::class.java)
        } catch (e: NoSuchMethodException) {
            throw e
        }
    }


    /**
     * Crea il view holder e collega gli eventi di default.
     *
     * Per modificare la classe dell'holder necessario fare l'override del metodo [instantiateViewHolder].
     * Usare questo metodo chiamando il super solo per aggiungere dei gesture listener in più.
     *
     * Di Default se l'holder implementa [ViewHolderWithClickGesture] e [defaultClickReceiver] è non null
     * viene aggiunto il click listener
     * Se l'holder implementa [ViewHolderWithLongClickGesture] e [defaultLongClickReceiver] non è nullo
     * viene aggiunto il long click listener
     * @param viewGroup
     * @param viewLayout
     * @return
     */
    @CallSuper
    override fun onCreateViewHolder(viewGroup: ViewGroup, @LayoutRes viewLayout: Int): ViewHolderClass {
        val viewHolderClass = instantiateViewHolder(viewGroup, viewLayout)

        addDefaultGestureListenerTo(viewHolderClass)

        return viewHolderClass
    }


    /**
     * Metodo per istanziare il [android.support.v7.widget.RecyclerView.ViewHolder]
     * questo viene chiamato da [onCreateViewHolder]
     * @param viewGroup
     * @param viewLayout
     * @return ViewHolder da istanziare
     */
    protected open fun instantiateViewHolder(viewGroup: ViewGroup, @LayoutRes viewLayout: Int): ViewHolderClass {
        val view = inflater.inflate(viewLayout, viewGroup, false)
        try {
            return viewHolderClassConstructor.newInstance(view)
        } catch (e: Exception) {
            throw e
        }
    }

    /**
     * Return the view layout of the item at `position` for the purposes
     * of view recycling.
     *
     * The default implementation of this method returns layoutResource

     * @param position position to query
     * *
     * @return integer value identifying the layout of the view needed to represent the item at
     * `position`. Type codes need not be contiguous.
     */
    @LayoutRes
    override fun getItemViewType(position: Int): Int = layoutResource

    /**
     * Possibilità di aggiungere gestures alle view del view holder
     * Questo viene chiamato sempre all'OnCreateViewHolder e controlla se l'holder implementa una certa interfaccia allora chiama il [Interceptor]
     * che gestirà il listener
     */
    protected fun addDefaultGestureListenerTo(holder: RecyclerView.ViewHolder) {
        if (holder is ViewHolderWithClickGesture && defaultClickReceiver != null)
            addClick(defaultClickReceiver!!, holder.viewWithClick(), holder)

        if (holder is ViewHolderWithLongClickGesture && defaultLongClickReceiver != null)
            addLongClick(defaultLongClickReceiver!!, holder.viewWithLongClick(), holder = holder)
    }

    protected fun addClick(receiver: ClickReceiver<ObjectClass>,
                           view: View,
                           holder: RecyclerView.ViewHolder) {
        view.setOnClickListener { v ->
            val item = getItem(holder.adapterPosition)
            if (item != null) {
                receiver.onViewClicked(holder.itemView.parent as RecyclerView,
                        v, holder.adapterPosition,
                        item)
            }
        }
    }

    protected fun addLongClick(receiver: LongClickReceiver<ObjectClass>,
                               view: View,
                               holder: RecyclerView.ViewHolder,
                               consumeTouch: Boolean = false) {
        view.setOnLongClickListener { v ->
            val item = getItem(holder.adapterPosition)
            if (item != null) {
                receiver.onViewLongClicked(holder.itemView.parent as RecyclerView,
                        v,
                        holder.adapterPosition,
                        item)
            }

            consumeTouch
        }
    }

    fun swapList(newList: List<ObjectClass>?, notifyDataSetChanged: Boolean) {
        val previousItemsCount = itemCount

        items.clear()
        newList?.let {
            items.addAll(it)
        }

        val newItemsCount = itemCount

        if (notifyDataSetChanged && (newItemsCount != 0 || previousItemsCount != 0)) {
            when {
                previousItemsCount > newItemsCount -> {
                    notifyItemRangeChanged(0, newItemsCount)
                    notifyItemRangeRemoved(newItemsCount, previousItemsCount - newItemsCount)
                }
                newItemsCount > previousItemsCount -> {
                    notifyItemRangeChanged(0, previousItemsCount)
                    notifyItemRangeInserted(previousItemsCount, newItemsCount - previousItemsCount)
                }
                else -> notifyItemRangeChanged(0, previousItemsCount)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    open fun getItem(position: Int): ObjectClass? =
            if (position != -1 && position < itemCount) items[position] else null

    fun indexOf(item: ObjectClass): Int = items.indexOf(item)

    /**
     * Callback per il click nelle view dei viewholder di una recycleview
     */
    interface ClickReceiver<in ObjectClass> {

        /**
         * @param recyclerView  recycler view da cui arriva l'evento
         * @param view     view corrente
         * @param position posizione dell item
         * @param item     item corrente
         */
        fun onViewClicked(recyclerView: RecyclerView, view: View, position: Int, item: ObjectClass)
    }

    /**
     * Callback per il long click nelle view dei viewholder di una recycleview
     */
    interface LongClickReceiver<in ObjectClass> {

        /**
         * @param recyclerView  recycler view da cui arriva l'evento
         * @param view     view corrente
         * @param position posizione corrente
         * @param item     item corrente
         */
        fun onViewLongClicked(recyclerView: RecyclerView, view: View, position: Int, item: ObjectClass)
    }
}