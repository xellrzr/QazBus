package com.revolage.quzbus.data.repositoryImpl

import com.revolage.quzbus.data.sharedpref.AppSharedPreferences
import com.revolage.quzbus.domain.repository.IconRepository
import javax.inject.Inject

class IconRepositoryImpl @Inject constructor(
    private val pref:AppSharedPreferences
) : IconRepository {
    override fun setIconId(iconId: Int) {
        pref.setIconId(iconId)
    }

    override fun getIconId(): Int {
        return pref.getIconId()
    }


}