package com.example.lenovo.graduworkapp;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Lenovo on 2018/3/4.
 */
public class DiabeteLab {
    //app在内存里活多久，单例就存活多久，不受activity、fragment生命周期影响
    private static DiabeteLab sDiabeteLab;
    //空list保存Diabete对象
    private List<Diabete> mDiabetes;
    //diy的diabete对象
    private Diabete mDiyDiabete;
    //储存正在制作的那个糖人对象
    private Diabete currentDiabete;

    private Context mContext;

    public static DiabeteLab get(Context context) {
        if (sDiabeteLab == null) {
            sDiabeteLab = new DiabeteLab(context);
        }
        return sDiabeteLab;
    }

    private DiabeteLab(Context context) {
        mDiabetes = new ArrayList<>();
        mContext = context;

        //生成20个Diabete对象
        for (int i = 0; i < 3; i++) {
            Diabete diabete = new Diabete("Diabete#" + i);
            diabete.setMade(false);
            diabete.setNoId(-1);
            switch (i) {
                case 0:
                    diabete.setName("pokemonLogoo");
                    diabete.setDrawId(R.mipmap.diabete_1);
                    diabete.setNoId(1);
                    break;
                case 1:
                    diabete.setName("tankDiabete");
                    diabete.setDrawId(R.mipmap.diabete_2);
                    diabete.setNoId(2);
                    break;
                case 2:
                    diabete.setDrawId(R.mipmap.diabete_3);
                    diabete.setNoId(3);
                    break;
                default:
                    break;
            }
            mDiabetes.add(diabete);
        }
        mDiyDiabete = new Diabete("DiyDiabete");
        mDiyDiabete.setMade(false);
        mDiyDiabete.setNoId(-1);
    }

    public List<Diabete> getDiabetes() {
        return mDiabetes;
    }

    public Diabete getDiabete(UUID id) {
        for (Diabete diabete : mDiabetes) {
            if (diabete.getId().equals(id)) {
                return diabete;
            }
        }
        return null;
    }

    public Diabete getCurrentDiabete() {
        return currentDiabete;
    }

    public void setCurrentDiabete(Diabete diabete) {
        currentDiabete = diabete;
    }

    /**
     * 专门给diy糖人用的修改当前制作糖人的方法
     */
    public void setCurrentDiabeteDIYID() {
        if (getCurrentDiabete() != null) {
            getCurrentDiabete().setMade(false);
        }
        setCurrentDiabete(mDiyDiabete);
        mDiyDiabete.setMade(true);
    }
}