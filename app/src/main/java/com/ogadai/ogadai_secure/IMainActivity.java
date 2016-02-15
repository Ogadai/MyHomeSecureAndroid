package com.ogadai.ogadai_secure;

/**
 * Created by alee on 15/02/2016.
 */
public interface IMainActivity {
    void createAndShowDialogFromTask(final Exception exception, final String title);
    void createAndShowDialogFromTask(final String message, final String title);
    void createAndShowDialog(final Exception exception, final String title);
    void createAndShowDialog(final String message, final String title);

    void showProgressBar();
    void hideProgressBar();
}
