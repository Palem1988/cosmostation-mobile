package wannabit.io.cosmostaion.fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

import wannabit.io.cosmostaion.R;
import wannabit.io.cosmostaion.activities.SendActivity;
import wannabit.io.cosmostaion.base.BaseChain;
import wannabit.io.cosmostaion.base.BaseConstant;
import wannabit.io.cosmostaion.base.BaseFragment;
import wannabit.io.cosmostaion.dialog.Dialog_Fee_Description;
import wannabit.io.cosmostaion.model.type.Coin;
import wannabit.io.cosmostaion.model.type.Fee;
import wannabit.io.cosmostaion.utils.WDp;
import wannabit.io.cosmostaion.utils.WLog;

import static wannabit.io.cosmostaion.base.BaseConstant.FEE_BNB_SEND;
import static wannabit.io.cosmostaion.base.BaseConstant.IS_TEST;

public class SendStep3Fragment extends BaseFragment implements View.OnClickListener {

    public final static int SELECT_GAS_DIALOG = 6001;
    public final static int SELECT_FREE_DIALOG = 6002;

    private RelativeLayout  mBtnGasType;
    private TextView        mTvGasType;

    private LinearLayout    mFeeLayer1;
    private TextView        mMinFeeAmount;
    private TextView        mMinFeePrice;

    private LinearLayout    mFeeLayer2;
    private TextView        mGasAmount;
    private TextView        mGasRate;
    private TextView        mGasFeeAmount;
    private TextView        mGasFeePrice;

    private LinearLayout    mFeeLayer3;
    private SeekBar         mSeekBarGas;

    private LinearLayout    mSpeedLayer;
    private ImageView       mSpeedImg;
    private TextView        mSpeedMsg;

    private Button          mBeforeBtn, mNextBtn;

    private BigDecimal      mAvailable      = BigDecimal.ZERO;      // uatom scale
    private BigDecimal      mToSend         = BigDecimal.ZERO;      // uatom scale
    private BigDecimal      mFeeAmount      = BigDecimal.ZERO;      // uatom scale
    private BigDecimal      mFeePrice       = BigDecimal.ZERO;

    public static SendStep3Fragment newInstance(Bundle bundle) {
        SendStep3Fragment fragment = new SendStep3Fragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tx_step_fee, container, false);
        mBtnGasType     = rootView.findViewById(R.id.btn_gas_type);
        mTvGasType      = rootView.findViewById(R.id.gas_type);

        mFeeLayer1      = rootView.findViewById(R.id.fee_dp_layer1);
        mMinFeeAmount   = rootView.findViewById(R.id.min_fee_amount);
        mMinFeePrice    = rootView.findViewById(R.id.min_fee_price);

        mFeeLayer2      = rootView.findViewById(R.id.fee_dp_layer2);
        mGasAmount      = rootView.findViewById(R.id.gas_amount);
        mGasRate        = rootView.findViewById(R.id.gas_rate);
        mGasFeeAmount   = rootView.findViewById(R.id.gas_fee);
        mGasFeePrice    = rootView.findViewById(R.id.gas_fee_price);

        mFeeLayer3      = rootView.findViewById(R.id.fee_dp_layer3);
        mSeekBarGas     = rootView.findViewById(R.id.gas_price_seekbar);

        mSpeedLayer     = rootView.findViewById(R.id.speed_layer);
        mSpeedImg       = rootView.findViewById(R.id.speed_img);
        mSpeedMsg       = rootView.findViewById(R.id.speed_txt);

        mBeforeBtn      = rootView.findViewById(R.id.btn_before);
        mNextBtn        = rootView.findViewById(R.id.btn_next);
        mBeforeBtn.setOnClickListener(this);
        mNextBtn.setOnClickListener(this);
        WDp.DpMainDenom(getContext(), getSActivity().mAccount.baseChain, mTvGasType);

        if (getSActivity().mBaseChain.equals(BaseChain.COSMOS_MAIN)) {
            mBtnGasType.setOnClickListener(this);
            mSpeedLayer.setOnClickListener(this);
            Rect bounds = mSeekBarGas.getProgressDrawable().getBounds();
            mSeekBarGas.setProgressDrawable(getResources().getDrawable(R.drawable.gas_atom_seekbar_style));
            mSeekBarGas.getProgressDrawable().setBounds(bounds);
            mSeekBarGas.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        onUpdateFeeLayer();
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) { }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) { }
            });
            mSeekBarGas.setProgress(0);

        } else if (getSActivity().mBaseChain.equals(BaseChain.IRIS_MAIN)) {
            mFeeLayer1.setVisibility(View.GONE);
            mFeeLayer2.setVisibility(View.VISIBLE);
            mFeeLayer3.setVisibility(View.GONE);

            mSpeedImg.setImageDrawable(getResources().getDrawable(R.drawable.fee_img));
            mSpeedMsg.setText(getString(R.string.str_fee_speed_title_iris));

            mGasAmount.setText(BaseConstant.FEE_IRIS_GAS_AMOUNT_SEND);
            mGasRate.setText(WDp.getDpString(BaseConstant.FEE_IRIS_GAS_RATE_AVERAGE, 6));
            mFeeAmount = new BigDecimal(BaseConstant.FEE_IRIS_GAS_AMOUNT_SEND).multiply(new BigDecimal(BaseConstant.FEE_IRIS_GAS_RATE_AVERAGE)).movePointRight(18).setScale(0);
            if(getBaseDao().getCurrency() != 5) {
                mFeePrice = WDp.attoToIris(mFeeAmount).multiply(new BigDecimal(""+getBaseDao().getLastIrisTic())).setScale(2, RoundingMode.DOWN);
            } else {
                mFeePrice = WDp.attoToIris(mFeeAmount).multiply(new BigDecimal(""+getBaseDao().getLastIrisTic())).setScale(8, RoundingMode.DOWN);
            }
            mGasFeeAmount.setText(WDp.getDpString(WDp.attoToIris(mFeeAmount).setScale(1).toPlainString(), 2));
            mGasFeePrice.setText(WDp.getPriceApproximatelyDp(getSActivity(), mFeePrice, getBaseDao().getCurrencySymbol(), getBaseDao().getCurrency()));

        } else if (getSActivity().mBaseChain.equals(BaseChain.BNB_MAIN)) {
            mFeeLayer1.setVisibility(View.VISIBLE);
            mFeeLayer2.setVisibility(View.GONE);
            mFeeLayer3.setVisibility(View.GONE);

            mSpeedImg.setImageDrawable(getResources().getDrawable(R.drawable.fee_img));
            mSpeedMsg.setText(getString(R.string.str_fee_speed_title_bnb));

            mMinFeeAmount.setText(WDp.getDpString(FEE_BNB_SEND, 8));
            if(getBaseDao().getCurrency() != 5) {
                mFeePrice = new BigDecimal(FEE_BNB_SEND).multiply(new BigDecimal(""+getBaseDao().getLastBnbTic())).setScale(2, RoundingMode.DOWN);
            } else {
                mFeePrice = new BigDecimal(FEE_BNB_SEND).multiply(new BigDecimal(""+getBaseDao().getLastBnbTic())).setScale(8, RoundingMode.DOWN);
            }
            mMinFeePrice.setText(WDp.getPriceApproximatelyDp(getSActivity(), mFeePrice, getBaseDao().getCurrencySymbol(), getBaseDao().getCurrency()));

        }
        return rootView;
    }

    @Override
    public void onRefreshTab() {
        super.onRefreshTab();
        if (getSActivity().mBaseChain.equals(BaseChain.COSMOS_MAIN)) {
            mAvailable  = getSActivity().mAccount.getAtomBalance();
            mToSend     = new BigDecimal(getSActivity().mTargetCoins.get(0).amount);
            onUpdateFeeLayer();
        }
    }

    @Override
    public void onClick(View v) {
        if(v.equals(mBeforeBtn)) {
            getSActivity().onBeforeStep();

        } else if (v.equals(mNextBtn)) {
            if (getSActivity().mBaseChain.equals(BaseChain.COSMOS_MAIN)) {
                if(IS_TEST) {
                    Fee fee = new Fee();
                    Coin gasCoin = new Coin();
                    gasCoin.denom = BaseConstant.COSMOS_MUON;
                    gasCoin.amount = mFeeAmount.toPlainString();
                    ArrayList<Coin> amount = new ArrayList<>();
                    amount.add(gasCoin);
                    fee.amount = amount;
                    fee.gas = BaseConstant.FEE_GAS_AMOUNT_HALF;
                    getSActivity().mTargetFee = fee;
                } else {
                    Fee fee = new Fee();
                    Coin gasCoin = new Coin();
                    gasCoin.denom = BaseConstant.COSMOS_ATOM;
                    gasCoin.amount = mFeeAmount.toPlainString();
                    ArrayList<Coin> amount = new ArrayList<>();
                    amount.add(gasCoin);
                    fee.amount = amount;
                    fee.gas = BaseConstant.FEE_GAS_AMOUNT_HALF;
                    getSActivity().mTargetFee = fee;
                }

            } else if (getSActivity().mBaseChain.equals(BaseChain.IRIS_MAIN)) {
                Fee fee = new Fee();
                Coin gasCoin = new Coin();
                gasCoin.denom = BaseConstant.COSMOS_IRIS_ATTO;
                gasCoin.amount = mFeeAmount.toPlainString();
                ArrayList<Coin> amount = new ArrayList<>();
                amount.add(gasCoin);
                fee.amount = amount;
                fee.gas = BaseConstant.FEE_IRIS_GAS_AMOUNT_SEND;
                getSActivity().mTargetFee = fee;

            } else if (getSActivity().mBaseChain.equals(BaseChain.BNB_MAIN)) {
                //TODO no need Fee set!!;
                Fee fee = new Fee();
                Coin gasCoin = new Coin();
                gasCoin.denom = BaseConstant.COSMOS_BNB;
                gasCoin.amount = FEE_BNB_SEND;
                ArrayList<Coin> amount = new ArrayList<>();
                amount.add(gasCoin);
                fee.amount = amount;
                fee.gas = BaseConstant.FEE_GAS_AMOUNT_AVERAGE;
                getSActivity().mTargetFee = fee;
            }
            getSActivity().onNextStep();

        } else if (v.equals(mBtnGasType)) {
            Toast.makeText(getContext(), getString(R.string.error_only_atom_for_fee), Toast.LENGTH_SHORT).show();

        }  else if (v.equals(mSpeedLayer)) {
            Bundle bundle = new Bundle();
            bundle.putInt("speed", mSeekBarGas.getProgress());
            Dialog_Fee_Description dialog = Dialog_Fee_Description.newInstance(bundle);
            dialog.setCancelable(true);
            dialog.show(getFragmentManager().beginTransaction(), "dialog");
        }
    }

    private void onUpdateFeeLayer() {
        if(mSeekBarGas.getProgress() == 0) {
            mSpeedImg.setImageDrawable(getResources().getDrawable(R.drawable.bycicle_img));
            mSpeedMsg.setText(getString(R.string.str_fee_speed_title_0));
            mFeeLayer1.setVisibility(View.VISIBLE);
            mFeeLayer2.setVisibility(View.GONE);

            mFeeAmount  = BigDecimal.ONE;
            if(getBaseDao().getCurrency() != 5) {
                mFeePrice = WDp.uAtomToAtom(mFeeAmount).multiply(new BigDecimal(""+getBaseDao().getLastAtomTic())).setScale(2, RoundingMode.DOWN);
            } else {
                mFeePrice = WDp.uAtomToAtom(mFeeAmount).multiply(new BigDecimal(""+getBaseDao().getLastAtomTic())).setScale(8, RoundingMode.DOWN);
            }

            mMinFeeAmount.setText(WDp.getDpString(WDp.uAtomToAtom(mFeeAmount).toPlainString(), 6));
            mMinFeePrice.setText(WDp.getPriceApproximatelyDp(getSActivity(), mFeePrice, getBaseDao().getCurrencySymbol(), getBaseDao().getCurrency()));


        } else if (mSeekBarGas.getProgress() == 1) {
            mSpeedImg.setImageDrawable(getResources().getDrawable(R.drawable.car_img));
            mSpeedMsg.setText(getString(R.string.str_fee_speed_title_1));
            mFeeLayer1.setVisibility(View.GONE);
            mFeeLayer2.setVisibility(View.VISIBLE);

            mGasAmount.setText(BaseConstant.FEE_GAS_AMOUNT_HALF);
            mGasRate.setText(WDp.getDpString(BaseConstant.FEE_GAS_RATE_LOW, 4));

            mFeeAmount = new BigDecimal(BaseConstant.FEE_GAS_AMOUNT_HALF).multiply(new BigDecimal(BaseConstant.FEE_GAS_RATE_LOW)).setScale(0);
            if(getBaseDao().getCurrency() != 5) {
                mFeePrice = WDp.uAtomToAtom(mFeeAmount).multiply(new BigDecimal(""+getBaseDao().getLastAtomTic())).setScale(2, RoundingMode.DOWN);
            } else {
                mFeePrice = WDp.uAtomToAtom(mFeeAmount).multiply(new BigDecimal(""+getBaseDao().getLastAtomTic())).setScale(8, RoundingMode.DOWN);
            }
            mGasFeeAmount.setText(WDp.getDpString(WDp.uAtomToAtom(mFeeAmount).toPlainString(), 6));
            mGasFeePrice.setText(WDp.getPriceApproximatelyDp(getSActivity(), mFeePrice, getBaseDao().getCurrencySymbol(), getBaseDao().getCurrency()));

//            WLog.w("mFeeAmount " + mFeeAmount);
//            WLog.w("mFeePrice " + mFeePrice);
//            WLog.w("WDp.uAtomToAtom(mFeeAmount) " + WDp.uAtomToAtom(mFeeAmount));


        } else if (mSeekBarGas.getProgress() == 2) {
            mSpeedImg.setImageDrawable(getResources().getDrawable(R.drawable.rocket_img));
            mSpeedMsg.setText(getString(R.string.str_fee_speed_title_2));
            mFeeLayer1.setVisibility(View.GONE);
            mFeeLayer2.setVisibility(View.VISIBLE);

            mGasAmount.setText(BaseConstant.FEE_GAS_AMOUNT_HALF);
            mGasRate.setText(WDp.getDpString(BaseConstant.FEE_GAS_RATE_AVERAGE, 3));

            mFeeAmount = new BigDecimal(BaseConstant.FEE_GAS_AMOUNT_HALF).multiply(new BigDecimal(BaseConstant.FEE_GAS_RATE_AVERAGE)).setScale(0);
            if(getBaseDao().getCurrency() != 5) {
                mFeePrice = WDp.uAtomToAtom(mFeeAmount).multiply(new BigDecimal(""+getBaseDao().getLastAtomTic())).setScale(2, RoundingMode.DOWN);
            } else {
                mFeePrice = WDp.uAtomToAtom(mFeeAmount).multiply(new BigDecimal(""+getBaseDao().getLastAtomTic())).setScale(8, RoundingMode.DOWN);
            }
            mGasFeeAmount.setText(WDp.getDpString(WDp.uAtomToAtom(mFeeAmount).toPlainString(), 6));
            mGasFeePrice.setText(WDp.getPriceApproximatelyDp(getSActivity(), mFeePrice, getBaseDao().getCurrencySymbol(), getBaseDao().getCurrency()));
        }


//        WLog.w("mAvailable "    + mAvailable.toPlainString());
//        WLog.w("mToSend "       + mToSend.toPlainString());
//        WLog.w("mFeeAmount "    + mFeeAmount.toPlainString());

        if((mToSend.add(mFeeAmount)).compareTo(mAvailable) > 0) {
            Toast.makeText(getContext(), getString(R.string.error_not_enough_fee), Toast.LENGTH_SHORT).show();
            mSeekBarGas.setProgress(mSeekBarGas.getProgress() - 1);
            onUpdateFeeLayer();
        }

    }

    private SendActivity getSActivity() {
        return (SendActivity)getBaseActivity();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == SELECT_FREE_DIALOG && resultCode == Activity.RESULT_OK) {
//            Fee result = new Fee();
//            ArrayList<Coin> amount = new ArrayList<>();
//            Coin testCoin = new Coin("uatom", "0");
//            amount.add(testCoin);
//            result.amount = amount;
//            result.gas = "200000";
//
//            getSActivity().mToDelegateFee = result;
//            getSActivity().onNextStep();
        }
    }

}