package wannabit.io.cosmostaion.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.squareup.picasso.Picasso;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import wannabit.io.cosmostaion.R;
import wannabit.io.cosmostaion.activities.MainActivity;
import wannabit.io.cosmostaion.activities.ValidatorListActivity;
import wannabit.io.cosmostaion.base.BaseChain;
import wannabit.io.cosmostaion.base.BaseFragment;
import wannabit.io.cosmostaion.model.type.Validator;
import wannabit.io.cosmostaion.network.ApiClient;
import wannabit.io.cosmostaion.network.res.ResKeyBaseUser;
import wannabit.io.cosmostaion.utils.WDp;
import wannabit.io.cosmostaion.utils.WUtil;

public class ValidatorOtherFragment extends BaseFragment {

    private SwipeRefreshLayout          mSwipeRefreshLayout;
    private RecyclerView                mRecyclerView;
    private OtherValidatorAdapter       mOtherValidatorAdapter;
    private TextView                    mValidatorSize;

    private ArrayList<Validator>        mMyValidators = new ArrayList<>();
    private ArrayList<Validator>        mOtherValidators = new ArrayList<>();

    public static ValidatorOtherFragment newInstance(Bundle bundle) {
        ValidatorOtherFragment fragment = new ValidatorOtherFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_validator_other, container, false);
        mSwipeRefreshLayout     = rootView.findViewById(R.id.layer_refresher);
        mRecyclerView           = rootView.findViewById(R.id.recycler);
        mValidatorSize          = rootView.findViewById(R.id.validator_cnt);

        mSwipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorPrimary));
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getMainActivity().onFetchAllData();
            }
        });

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getBaseActivity(), LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemViewCacheSize(20);
        mRecyclerView.setDrawingCacheEnabled(true);
        mOtherValidatorAdapter = new OtherValidatorAdapter();
        mRecyclerView.setAdapter(mOtherValidatorAdapter);

        return rootView;
    }

    @Override
    public void onRefreshTab() {
        if(!isAdded()) return;
        mOtherValidators    = getMainActivity().mOtherValidators;
        mMyValidators       = getMainActivity().mMyValidators;
        mValidatorSize.setText(""+mOtherValidators.size());
        WUtil.onSortByValidatorPower(mOtherValidators);

        mOtherValidatorAdapter.notifyDataSetChanged();
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onBusyFetch() {
        if(!isAdded()) return;
        mSwipeRefreshLayout.setRefreshing(false);
    }

    public ValidatorListActivity getMainActivity() {
        return (ValidatorListActivity)getBaseActivity();
    }

    private class OtherValidatorAdapter extends RecyclerView.Adapter<OtherValidatorAdapter.OtherValidatorHolder> {

        @NonNull
        @Override
        public OtherValidatorHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            return new OtherValidatorHolder(getLayoutInflater().inflate(R.layout.item_reward_validator, viewGroup, false));

        }

        @Override
        public void onBindViewHolder(@NonNull final OtherValidatorHolder holder, final int position) {
            final Validator validator  = mOtherValidators.get(position);

            if (getMainActivity().mBaseChain.equals(BaseChain.COSMOS_MAIN)) {
                holder.itemTvVotingPower.setText(WDp.getDpAmount(getContext(), new BigDecimal(validator.tokens), 6, BaseChain.getChain(getMainActivity().mAccount.baseChain)));
                holder.itemTvCommission.setText(WDp.getPercentDp(new BigDecimal(validator.commission.commission_rates.rate)));

            } else if (getMainActivity().mBaseChain.equals(BaseChain.IRIS_MAIN)) {
                holder.itemTvVotingPower.setText(WDp.getDpAmount(getContext(), new BigDecimal(validator.tokens).movePointRight(18), 6, BaseChain.getChain(getMainActivity().mAccount.baseChain)));
                holder.itemTvCommission.setText(WDp.getCommissionRate(validator.commission.rate));
            }

            holder.itemTvMoniker.setText(validator.description.moniker);
            holder.itemTvSubtitle.setText(R.string.str_commission);
            holder.itemFree.setVisibility(View.GONE);
            holder.itemRoot.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getMainActivity().onStartValidatorDetail(validator);
                }
            });

            holder.itemAvatar.setTag("imgv" + position);
            if(validator.keybaseInfo == null) {
                holder.itemAvatar.setImageDrawable(getResources().getDrawable(R.drawable.validator_none_img));
                if(!TextUtils.isEmpty(validator.description.identity)) {
                    ApiClient.getKeybaseService(getMainActivity()).getUserInfo("pictures", validator.description.identity).enqueue(new Callback<ResKeyBaseUser>() {
                        @Override
                        public void onResponse(Call<ResKeyBaseUser> call, final Response<ResKeyBaseUser> response) {
                            validator.keybaseInfo = response.body();
                            if(isAdded() && holder.itemAvatar.getTag().equals("imgv" + position)) {
                                try {
                                    Picasso.get()
                                            .load(response.body().getUrl())
                                            .fit()
                                            .placeholder(R.drawable.validator_none_img)
                                            .into(holder.itemAvatar);
                                }catch (Exception e) {}
                            }
                        }
                        @Override
                        public void onFailure(Call<ResKeyBaseUser> call, Throwable t) {}
                    });
                }

            } else {
                if(isAdded() && holder.itemAvatar.getTag().equals("imgv" + position)) {
                    try {
                        Picasso.get()
                                .load(validator.keybaseInfo.getUrl())
                                .fit()
                                .placeholder(R.drawable.validator_none_img)
                                .into(holder.itemAvatar);
                    }catch (Exception e) {}
                }
            }

            if(validator.jailed) {
                holder.itemAvatar.setBorderColor(getResources().getColor(R.color.colorRed));
                holder.itemRevoked.setVisibility(View.VISIBLE);
            } else {
                holder.itemAvatar.setBorderColor(getResources().getColor(R.color.colorGray3));
                holder.itemRevoked.setVisibility(View.GONE);
            }

            if(checkIsMyValidator(mMyValidators, validator.description.moniker)) {
                if (getMainActivity().mBaseChain.equals(BaseChain.COSMOS_MAIN)) {
                    holder.itemRoot.setCardBackgroundColor(getResources().getColor(R.color.colorTransBg2));
                } else if (getMainActivity().mBaseChain.equals(BaseChain.IRIS_MAIN)) {
                    holder.itemRoot.setCardBackgroundColor(getResources().getColor(R.color.colorTransBg4));
                }
            } else {
                holder.itemRoot.setCardBackgroundColor(getResources().getColor(R.color.colorTransBg));
            }
        }


        private boolean checkIsMyValidator(ArrayList<Validator> userList, final String targetName){
            return FluentIterable.from(userList).anyMatch(new Predicate<Validator>() {
                @Override
                public boolean apply(@Nullable Validator input) {
                    return input.description.moniker.equals(targetName);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mOtherValidators.size();
        }

        public class OtherValidatorHolder extends RecyclerView.ViewHolder {
            CardView        itemRoot;
            CircleImageView itemAvatar;
            ImageView       itemRevoked;
            ImageView       itemFree;
            TextView        itemTvMoniker;
            TextView        itemTvVotingPower;
            TextView        itemTvSubtitle;
            TextView        itemTvCommission;

            public OtherValidatorHolder(@NonNull View itemView) {
                super(itemView);
                itemRoot            = itemView.findViewById(R.id.card_validator);
                itemAvatar          = itemView.findViewById(R.id.avatar_validator);
                itemRevoked         = itemView.findViewById(R.id.avatar_validator_revoke);
                itemFree            = itemView.findViewById(R.id.avatar_validator_free);
                itemTvMoniker       = itemView.findViewById(R.id.moniker_validator);
                itemTvVotingPower   = itemView.findViewById(R.id.delegate_power_validator);
                itemTvSubtitle      = itemView.findViewById(R.id.subTitle2);
                itemTvCommission    = itemView.findViewById(R.id.delegate_commission_validator);
            }
        }
    }
}
