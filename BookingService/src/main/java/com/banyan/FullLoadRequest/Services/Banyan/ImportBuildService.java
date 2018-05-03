package com.banyan.FullLoadRequest.Services.Banyan;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.banyan.FullLoadRequest.Entities.Booking;
import com.banyan.FullLoadRequest.Repos.RateQtAddressRepository;
import com.banyan.FullLoadRequest.Repos.RateQtRepository;
import com.banyan.FullLoadRequest.Services.Booking.BookingBuilderService;
import com.banyan.FullLoadRequest.models.Booking.AuthenticationData;
import com.banyan.FullLoadRequest.models.Booking.FullLoad_Request;
import com.banyan.FullLoadRequest.models.Booking.ImportForBook_Request;
import com.banyan.FullLoadRequest.models.Booking.QuoteInformation;
import com.banyan.FullLoadRequest.models.enums.CurrencyTypes;

@Service
public class ImportBuildService {

	@Autowired
	QuoteInformation quoteInfo;
	@Autowired
	RateQtRepository qtRep;
	@Autowired
	FullLoad_Request fullLoad;
	@Autowired
	Booking books;
	@Autowired
	AuthenticationData authData;
	@Autowired
	RateQtAddressRepository addRepo;
	@Autowired
	BookingBuilderService bookService;

	private BigDecimal total = new BigDecimal(15);

	// ImportBookRequest without FullLoad
	public ImportForBook_Request buildImport(int id, boolean dispatch) {

		Booking books = bookService.getBooking(id);
		if (books == null) {
			System.out.println("ImportBook Request cannot be generated, Booking for id " + id + " does not exist in DB!");
			return null;
		}
		
		fullLoad = bookService.getFullLoad(books);
		if (fullLoad == null) {
			System.out.println("ImportBook Request cannot be generated, Booking for id "+id+" does not have required details!");
			return null;
			}

		authData = fullLoad.getAuthenticationData();
		String SCAC = books.getCARRIER_CODE();
		String carrierName = addRepo.findCarrierNameByCode(SCAC);
		String pro = fullLoad.getLoadinfo().getManifestID();
		String bol = fullLoad.getLoadinfo().getBOLNumber();
		CurrencyTypes currency = CurrencyTypes.US_Dollar;

		quoteInfo = new QuoteInformation.Builder().setQuoteID(0).setSCAC(SCAC).setTransitTime(0).setTotalCharge(total)
				.setFreightCharge(null).setFuelSurcharge(null).setDiscountPercentage(null).setDiscountAmount(null)
				.setAccessorialFees(null).setMinimum(null).setGrossCharge(null).setOtherCharges(null).setTariff(null)
				.setInterline(false).setMiles(0).setQuoteNumber(null).setCarrierPrice(null).setCustomerPrice(null)
				.setNote(null).setCurrencyID(currency.getValue()).build();

		ImportForBook_Request importBook = new ImportForBook_Request.Builder().setAuthenticationData(authData)
				.setQuoteInformation(quoteInfo).setPickupDateTime(null).setBOLNumber(bol).setDispatchLoad(dispatch)
				.setDispatchOverride(dispatch).setSubmitPickup(dispatch).setProNumber(pro).setActualCarrierName(carrierName)
				.setBillTo(fullLoad.getBillTo()).setLoadinfo(fullLoad.getLoadinfo())
				.setRateServices(fullLoad.getRateServices()).setProducts(fullLoad.getProducts())
				.setPackageInfo(fullLoad.getPackageInfo()).setShipper(fullLoad.getShipper())
				.setConsignee(fullLoad.getConsignee()).setShipperAccessorials(fullLoad.getShipperAccessorials())
				.setConsigneeAccessorials(fullLoad.getConsigneeAccessorials())
				.setLoadAccessorials(fullLoad.getLoadAccessorials()).setUserDefined(fullLoad.getUserDefined())
				.setReferenceField(fullLoad.getReferenceField()).build();

		return importBook;
	}
}
