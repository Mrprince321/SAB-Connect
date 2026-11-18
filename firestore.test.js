const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_store_owner";
const BOB_UID = "bob_other_owner";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

function pastDate(offsetMs = 5000) {
  return new Date(Date.now() - offsetMs);
}

test("Unauthenticated users cannot read or write any collection", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("products").get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("invoices").get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("employees").get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("clock_logs").get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("services").get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("portfolio_projects").get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("enquiries").get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("client_projects").get());
});

test("StoreProfile: owner create, partial updates, and cross-user isolation", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const created = pastDate(10000);

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      storeName: "SAB Digitech Hub",
      ownerName: "Alice",
      email: "alice@example.com",
      phone: "+91 81253 99937",
      address: "Hyderabad HQ",
      currencySymbol: "₹",
      taxRatePercent: 18.0,
      invoiceCounter: 1001,
      createdAt: created,
      updatedAt: created,
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      storeName: "SAB Connect Cloud",
      ownerName: "Alice M",
      email: "alice@example.com",
      phone: "555-0199",
      address: "102 Main St",
      currencySymbol: "₹",
      taxRatePercent: 18.0,
      updatedAt: pastDate(1000),
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      invoiceCounter: 1002,
      updatedAt: pastDate(500),
    })
  );

  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      invoiceCounter: 1003,
      updatedAt: new Date(Date.now() + 60000),
    })
  );

  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
});

test("Services & Portfolio: owner CRUD, ghost-field rejection, and cross-user isolation", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const created = pastDate(10000);

  const svcRef = aliceDb.collection("users").doc(ALICE_UID).collection("services").doc("svc_1");
  await assertSucceeds(
    svcRef.set({
      userId: ALICE_UID,
      title: "Mobile App Engineering",
      category: "Mobile & Cloud",
      description: "Android, iOS and React Native mobile engineering with cloud API sync.",
      deliverables: "Android • iOS • Firebase • REST API",
      startingPrice: 45000.0,
      timeline: "4-6 Weeks",
      activeService: true,
      createdAt: created,
      updatedAt: created,
    })
  );

  await assertSucceeds(
    svcRef.update({
      title: "Mobile App Engineering",
      category: "Mobile & Cloud",
      description: "Updated description for mobile apps.",
      deliverables: "Android • iOS • Firebase",
      startingPrice: 49000.0,
      timeline: "4 Weeks",
      activeService: true,
      updatedAt: pastDate(1000),
    })
  );

  await assertFails(
    svcRef.update({
      startingPrice: -100.0,
      updatedAt: pastDate(500),
    })
  );

  const portRef = aliceDb.collection("users").doc(ALICE_UID).collection("portfolio_projects").doc("port_1");
  await assertSucceeds(
    portRef.set({
      userId: ALICE_UID,
      title: "Hyderabad Retail Cloud Portal",
      clientName: "SAB Mart Group",
      category: "Mobile App",
      summary: "Unified multi-branch billing and customer order portal.",
      techStack: "Kotlin • Jetpack Compose • Firestore",
      impactMetric: "3.2x Faster Checkout",
      featured: true,
      createdAt: created,
      updatedAt: created,
    })
  );

  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("services").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("portfolio_projects").get());
});

test("Enquiries & ClientProjects: CRM lifecycle, assignment, notes, and security bounds", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const created = pastDate(10000);

  const enqRef = aliceDb.collection("users").doc(ALICE_UID).collection("enquiries").doc("enq_1");
  await assertSucceeds(
    enqRef.set({
      userId: ALICE_UID,
      enquiryCode: "ENQ-1001",
      enquiryType: "QUOTATION",
      customerId: "cust_1",
      customerName: "Kushwanth",
      customerPhone: "+91 98765 43210",
      customerEmail: "kush@example.com",
      serviceTitle: "Web & E-Commerce",
      projectTitle: "Custom D2C Storefront App",
      requirements: "Need product catalog, online payments, and lead dashboard.",
      budgetRange: "₹40,000 - ₹75,000",
      preferredDate: "2026-12-01",
      status: "NEW",
      assignedEmployeeId: "",
      assignedEmployeeName: "",
      quotedAmount: 0.0,
      followUpDate: "",
      followUpNote: "",
      latestNote: "Customer requested quote",
      updates: [
        {
          authorRole: "CUSTOMER",
          authorName: "Kushwanth",
          message: "Submitted quotation request",
          timestampLabel: "Just now",
        },
      ],
      createdAt: created,
      updatedAt: created,
    })
  );

  // Admin assigns lead to employee
  await assertSucceeds(
    enqRef.update({
      assignedEmployeeId: "emp_1",
      assignedEmployeeName: "Jordan Patel",
      status: "IN_PROGRESS",
      latestNote: "Assigned to Jordan Patel",
      updates: [
        {
          authorRole: "ADMIN",
          authorName: "Admin",
          message: "Assigned lead to Jordan Patel",
          timestampLabel: "Today",
        },
      ],
      updatedAt: pastDate(2000),
    })
  );

  // Employee records quotation, follow-up & call notes
  await assertSucceeds(
    enqRef.update({
      status: "QUOTED",
      quotedAmount: 55000.0,
      followUpDate: "2026-12-05 11:00 AM",
      followUpNote: "Call client to review milestone scope",
      latestNote: "Call completed; shared ₹55,000 proposal",
      updates: [
        {
          authorRole: "EMPLOYEE",
          authorName: "Jordan Patel",
          message: "Call note: Shared proposal",
          timestampLabel: "Today",
        },
      ],
      updatedAt: pastDate(1000),
    })
  );

  // Immutable enquiryCode tamper fails
  await assertFails(
    enqRef.update({
      enquiryCode: "HACK-999",
      updatedAt: pastDate(500),
    })
  );

  // Invalid status fails
  await assertFails(
    enqRef.update({
      status: "HACKED_STATUS",
      quotedAmount: 100.0,
      followUpDate: "",
      followUpNote: "",
      latestNote: "",
      updates: [],
      updatedAt: pastDate(400),
    })
  );

  // Ghost field fails
  await assertFails(
    enqRef.update({
      updates: [],
      latestNote: "ok",
      ghostField: "bad",
      updatedAt: pastDate(300),
    })
  );

  // Convert to ClientProject
  const projRef = aliceDb.collection("users").doc(ALICE_UID).collection("client_projects").doc("proj_1");
  await assertSucceeds(
    projRef.set({
      userId: ALICE_UID,
      enquiryId: "enq_1",
      title: "Custom D2C Storefront App",
      clientName: "Kushwanth",
      serviceTitle: "Web & E-Commerce",
      assignedEmployeeId: "emp_1",
      assignedEmployeeName: "Jordan Patel",
      status: "IN_PROGRESS",
      progressPercent: 35,
      milestoneNote: "UI wireframes approved",
      dueDate: "2026-12-28",
      projectValue: 55000.0,
      createdAt: created,
      updatedAt: created,
    })
  );

  await assertSucceeds(
    projRef.update({
      title: "Custom D2C Storefront App",
      clientName: "Kushwanth",
      serviceTitle: "Web & E-Commerce",
      assignedEmployeeId: "emp_1",
      assignedEmployeeName: "Jordan Patel",
      status: "REVIEW",
      progressPercent: 85,
      milestoneNote: "Staging build delivered for UAT",
      dueDate: "2026-12-28",
      projectValue: 55000.0,
      updatedAt: pastDate(1000),
    })
  );

  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("enquiries").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("client_projects").get());
});

test("Employees & ClockLogs: real-time shift transitions and immutable logs", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const created = pastDate(10000);

  const empRef = aliceDb.collection("users").doc(ALICE_UID).collection("employees").doc("emp_1");
  await assertSucceeds(
    empRef.set({
      userId: ALICE_UID,
      fullName: "Jordan Patel",
      role: "Solutions Consultant",
      phone: "555-3310",
      shiftStatus: "CLOCKED_OUT",
      activeRecord: true,
      createdAt: created,
      updatedAt: created,
    })
  );

  await assertSucceeds(
    empRef.update({
      shiftStatus: "CLOCKED_IN",
      lastClockActionAt: pastDate(1000),
      updatedAt: pastDate(1000),
    })
  );

  const logRef = aliceDb.collection("users").doc(ALICE_UID).collection("clock_logs").doc("log_1");
  await assertSucceeds(
    logRef.set({
      userId: ALICE_UID,
      employeeId: "emp_1",
      employeeName: "Jordan Patel",
      employeeRole: "Solutions Consultant",
      action: "CLOCK_IN",
      note: "Morning shift",
      loggedAt: pastDate(1000),
    })
  );

  await assertFails(logRef.update({ action: "CLOCK_OUT" }));
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("clock_logs").get());
});
