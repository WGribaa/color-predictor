package com.wholebrain.colorpredictor;

import java.io.File;
import java.util.List;

public interface FxObserver {
    void updateRecentFiles(List<File> files);
    void updateSelectedOperator(int index);
}
