package io.agora.chatdemo.contact;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.List;

import io.agora.chat.Presence;
import io.agora.chat.uikit.models.EaseUser;
import io.agora.chatdemo.DemoHelper;
import io.agora.chatdemo.contact.viewmodels.ContactsListViewModel;
import io.agora.chatdemo.conversation.viewmodel.PresenceViewModel;
import io.agora.chatdemo.general.callbacks.OnResourceParseCallback;
import io.agora.chatdemo.general.constant.DemoConstant;
import io.agora.chatdemo.general.livedatas.EaseEvent;
import io.agora.chatdemo.general.livedatas.LiveDataBus;

public class ContactListFragment extends BaseContactListFragment<EaseUser> {
    private ContactsListViewModel mContactListViewModel;
    protected PresenceViewModel presenceViewModel;
    protected List<EaseUser> mData = new ArrayList<>();
    private String searchKey;

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        etSearch.setVisibility(View.VISIBLE);
    }

    @Override
    protected void initViewModel() {
        super.initViewModel();
        initContactsListViewModel();
        initPresenceViewModel();
    }

    protected void initPresenceViewModel() {
        presenceViewModel= new ViewModelProvider(this).get(PresenceViewModel.class);
        presenceViewModel.presencesObservable().observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<List<Presence>>() {
                @Override
                public void onSuccess(@Nullable List<Presence> data) {
                    ((ContactListAdapter) mListAdapter).setPresences(DemoHelper.getInstance().getPresences());
                    checkView(etSearch.getText().toString().trim());
                }
            });
        });
    }

    private void initContactsListViewModel() {
        mContactListViewModel = new ViewModelProvider(this).get(ContactsListViewModel.class);
        mContactListViewModel.getContactObservable().observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<List<EaseUser>>() {
                @Override
                public void onSuccess(List<EaseUser> data) {
                    srlContactRefresh.setRefreshing(false);
                    if(!TextUtils.isEmpty(searchKey)) {
                        searchText(searchKey);
                        return;
                    }
                    mListAdapter.setData(data);
                    mData = data;
                    presenceViewModel.subscribePresences(data, 7 * 24 * 60 * 60);
                }

                @Override
                public void onLoading(@Nullable List<EaseUser> data) {
                    super.onLoading(data);
                    if(data!=null&&data.size()>0) {
                        mData = data;
                        mListAdapter.setData(mData);
                        checkView(etSearch.getText().toString().trim());
                    }
                }

                @Override
                public void onError(int code, String message) {
                    super.onError(code, message);
                    runOnUiThread(() -> srlContactRefresh.setRefreshing(false));
                }
            });
        });
        mContactListViewModel.resultObservable().observe(getViewLifecycleOwner(), response -> {
            parseResource(response, new OnResourceParseCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean data) {
                    mContactListViewModel.loadContactList(false);
                }
            });
        });

        mContactListViewModel.deleteObservable().observe(getViewLifecycleOwner(), response -> {
            parseResource(response, new OnResourceParseCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean data) {
                    mContactListViewModel.loadContactList(false);
                }
            });
        });

        mContactListViewModel.getSearchObservable().observe(getViewLifecycleOwner(), result -> {
            parseResource(result, new OnResourceParseCallback<List<EaseUser>>() {
                @Override
                public void onSuccess(@Nullable List<EaseUser> data) {
                    mListAdapter.setData(data);
                    presenceViewModel.subscribePresences(data, 7 * 24 * 60 * 60);
                }
            });
        });

        mContactListViewModel.messageChangeObservable().with(DemoConstant.CONTACT_CHANGE, EaseEvent.class).observe(getViewLifecycleOwner(), event -> {
            if (event == null) {
                return;
            }
            if (event.isContactChange()) {
                mContactListViewModel.loadContactList(false);
            }
        });

        mContactListViewModel.messageChangeObservable().with(DemoConstant.REMOVE_BLACK, EaseEvent.class).observe(getViewLifecycleOwner(), event -> {
            if (event == null) {
                return;
            }
            if (event.isContactChange()) {
                mContactListViewModel.loadContactList(true);
            }
        });


        mContactListViewModel.messageChangeObservable().with(DemoConstant.CONTACT_ADD, EaseEvent.class).observe(getViewLifecycleOwner(), event -> {
            if (event == null) {
                return;
            }
            if (event.isContactChange()) {
                mContactListViewModel.loadContactList(false);
            }
        });


        mContactListViewModel.messageChangeObservable().with(DemoConstant.CONTACT_DELETE, EaseEvent.class).observe(getViewLifecycleOwner(), event -> {
            if (event == null) {
                return;
            }
            if (event.isContactChange()) {
                mContactListViewModel.loadContactList(false);
            }
        });

        mContactListViewModel.messageChangeObservable().with(DemoConstant.CONTACT_UPDATE, EaseEvent.class).observe(getViewLifecycleOwner(), event -> {
            if (event == null) {
                return;
            }
            if (event.isContactChange()) {
                mContactListViewModel.loadContactList(false);
            }
        });
    }

    @Override
    protected void initListener() {
        super.initListener();
        LiveDataBus.get().with(DemoConstant.PRESENCES_CHANGED).observe(getViewLifecycleOwner(), event -> {
            ((ContactListAdapter) mListAdapter).setPresences(DemoHelper.getInstance().getPresences());
            mListAdapter.setData(mData);
        });
    }

    @Override
    protected void initData() {
        super.initData();
        mContactListViewModel.loadContactList(true);
    }

    @Override
    public void onRefresh() {
        super.onRefresh();
        mContactListViewModel.loadContactList(true);
    }

    @Override
    public void onItemClick(View view, int position) {
        ContactDetailActivity.actionStart(mContext, mListAdapter.getData().get(position).getUsername());
    }

    @Override
    protected void searchText(String content) {
        searchKey = content;
        checkSearchContent(content);
        checkView(content);
    }

    protected void checkSearchContent(String content) {
        if (TextUtils.isEmpty(content)) {
            mContactListViewModel.loadContactList(false);
        } else {
            mContactListViewModel.searchContact(content);
        }
    }

    protected void checkView(String content) {
        if (TextUtils.isEmpty(content)) {
            sideBarContact.setVisibility(View.VISIBLE);
            srlContactRefresh.setEnabled(true);
        } else {
            sideBarContact.setVisibility(View.GONE);
            srlContactRefresh.setEnabled(false);
        }
    }

}
