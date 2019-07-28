package com.wholebrain.colorpredictor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public interface FxObserver {
    void updateRecentFiles(List<File> files);
    void updateSelectedOperator(int index);
}
