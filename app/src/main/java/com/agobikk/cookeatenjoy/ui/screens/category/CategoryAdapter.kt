package com.agobikk.cookeatenjoy.ui.screens.category

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.agobikk.cookeatenjoy.R
import com.agobikk.cookeatenjoy.databinding.LayoutCategoryListItemBinding
import com.agobikk.cookeatenjoy.model.Category
import com.agobikk.cookeatenjoy.ui.screens.category.ChooseCategoryDish.chooseDishOfType
import com.bumptech.glide.Glide
import timber.log.Timber

class CategoryAdapter(private val onCategoryClickListener: OnCategoryClickListener) :
    RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {
    var categoryData = listOf<Category>()
        set(value) {
            field = value
            notifyItemChanged(itemCount)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder =
        CategoryViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.layout_category_list_item, parent, false)
        )

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val item = categoryData[position]
        holder.itemView.setOnClickListener {
            onCategoryClickListener.onClick(item)
        }
        holder.bind(onCategoryClickListener, item)

    }

    override fun getItemCount() = categoryData.size

    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val viewBinding: LayoutCategoryListItemBinding by viewBinding()

        fun bind(onCategoryClickListener: OnCategoryClickListener, item: Category) =
            with(viewBinding) {
                categoryTitle.text = item.categoryName
                categoryImage.apply {
                    Glide
                        .with(context)
                        .load(item.imageUrl)
                        .into(this)
                }
                itemView.setOnClickListener {
                    onCategoryClickListener.onClick(item)
                    chooseDishOfType = item.categoryName
                    Timber.d("onBindViewHolder>>>>>>>>:${item.categoryName}")
                }
            }

    }

}

object ChooseCategoryDish {
    var chooseDishOfType: String = ""
}

